package com.laboussole.domain.model.notification;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.monitoring.MonitoringEventId;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * One alert to deliver, to one recipient, over one channel — persisted the
 * moment the monitoring event is saved, in the same transaction.
 *
 * <p>This is the durability guarantee: the intent to alert survives a crash,
 * a broker outage or a redeploy, because it is committed atomically with the
 * event that justifies it. Nothing is dispatched from memory, and nothing is
 * dispatched for an event whose transaction rolled back.
 *
 * <p>Retries use exponential backoff. A channel that keeps failing does not
 * spin: it doubles its wait until the attempt budget is spent, then lands in
 * {@link AlertDeliveryStatus#DEAD_LETTER} where it stays visible instead of
 * disappearing.
 */
public final class AlertDelivery {

    /** Error text kept per attempt; long stack traces are truncated to this. */
    public static final int MAX_ERROR_LENGTH = 500;

    private final AlertDeliveryId id;
    private final MonitoringEventId eventId;
    private final UserId recipientId;
    private final AlertChannel channel;
    private AlertDeliveryStatus status;
    private int attemptCount;
    private Instant nextAttemptAt;
    private String lastError;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deliveredAt;

    private AlertDelivery(
            AlertDeliveryId id,
            MonitoringEventId eventId,
            UserId recipientId,
            AlertChannel channel,
            AlertDeliveryStatus status,
            int attemptCount,
            Instant nextAttemptAt,
            String lastError,
            Instant createdAt,
            Instant updatedAt,
            Instant deliveredAt) {
        this.id = Objects.requireNonNull(id);
        this.eventId = Objects.requireNonNull(eventId);
        this.recipientId = Objects.requireNonNull(recipientId);
        this.channel = Objects.requireNonNull(channel);
        this.status = Objects.requireNonNull(status);
        this.attemptCount = attemptCount;
        this.nextAttemptAt = Objects.requireNonNull(nextAttemptAt);
        this.lastError = lastError;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.deliveredAt = deliveredAt;
    }

    /** A freshly queued delivery, due immediately. */
    public static AlertDelivery queue(
            MonitoringEventId eventId, UserId recipientId, AlertChannel channel, Instant now) {
        return new AlertDelivery(
                AlertDeliveryId.generate(), eventId, recipientId, channel,
                AlertDeliveryStatus.PENDING, 0, now, null, now, now, null);
    }

    public static AlertDelivery reconstitute(
            AlertDeliveryId id,
            MonitoringEventId eventId,
            UserId recipientId,
            AlertChannel channel,
            AlertDeliveryStatus status,
            int attemptCount,
            Instant nextAttemptAt,
            String lastError,
            Instant createdAt,
            Instant updatedAt,
            Instant deliveredAt) {
        return new AlertDelivery(id, eventId, recipientId, channel, status, attemptCount,
                nextAttemptAt, lastError, createdAt, updatedAt, deliveredAt);
    }

    /** Claims the delivery for a worker, so no other worker picks it up. */
    public void markInFlight(Instant now) {
        requireActive();
        this.status = AlertDeliveryStatus.IN_FLIGHT;
        this.attemptCount++;
        this.updatedAt = now;
    }

    public void markDelivered(Instant now) {
        requireActive();
        this.status = AlertDeliveryStatus.DELIVERED;
        this.deliveredAt = now;
        this.updatedAt = now;
        this.lastError = null;
    }

    /**
     * Records a failed attempt: schedules the next one with exponential
     * backoff, or dead-letters the delivery once the budget is spent.
     *
     * @param maxAttempts total attempts allowed, dead-letter beyond
     * @param baseBackoff wait after the first failure; doubles each time
     */
    public void recordFailure(String error, int maxAttempts, Duration baseBackoff, Instant now) {
        requireActive();
        this.lastError = truncate(error);
        this.updatedAt = now;
        if (attemptCount >= maxAttempts) {
            this.status = AlertDeliveryStatus.DEAD_LETTER;
            return;
        }
        this.status = AlertDeliveryStatus.PENDING;
        this.nextAttemptAt = now.plus(backoffAfter(attemptCount, baseBackoff));
    }

    /**
     * Returns a delivery stuck IN_FLIGHT — worker killed mid-dispatch — to the
     * queue so it is retried rather than lost.
     */
    public void releaseStaleClaim(Instant now) {
        if (status != AlertDeliveryStatus.IN_FLIGHT) {
            throw new IllegalStateException("Only an in-flight delivery can be released");
        }
        this.status = AlertDeliveryStatus.PENDING;
        this.nextAttemptAt = now;
        this.updatedAt = now;
        this.lastError = truncate("Reprise après interruption du worker pendant l'envoi.");
    }

    /**
     * Backoff after {@code attempt} failures: base × 2^(attempt-1).
     * With a 60 s base that is 1 min, 2 min, 4 min, 8 min…
     */
    private static Duration backoffAfter(int attempt, Duration baseBackoff) {
        long multiplier = 1L << Math.min(attempt - 1, 16);
        return baseBackoff.multipliedBy(multiplier);
    }

    private void requireActive() {
        if (status == AlertDeliveryStatus.DELIVERED || status == AlertDeliveryStatus.DEAD_LETTER) {
            throw new IllegalStateException(
                    "Delivery " + id.value() + " is already in terminal state " + status);
        }
    }

    private static String truncate(String error) {
        if (error == null) {
            return null;
        }
        return error.length() <= MAX_ERROR_LENGTH ? error : error.substring(0, MAX_ERROR_LENGTH);
    }

    public boolean isTerminal() {
        return status == AlertDeliveryStatus.DELIVERED || status == AlertDeliveryStatus.DEAD_LETTER;
    }

    public AlertDeliveryId id() { return id; }
    public MonitoringEventId eventId() { return eventId; }
    public UserId recipientId() { return recipientId; }
    public AlertChannel channel() { return channel; }
    public AlertDeliveryStatus status() { return status; }
    public int attemptCount() { return attemptCount; }
    public Instant nextAttemptAt() { return nextAttemptAt; }
    public Optional<String> lastError() { return Optional.ofNullable(lastError); }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Optional<Instant> deliveredAt() { return Optional.ofNullable(deliveredAt); }
}
