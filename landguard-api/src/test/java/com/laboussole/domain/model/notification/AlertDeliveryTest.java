package com.laboussole.domain.model.notification;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.monitoring.MonitoringEventId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertDeliveryTest {

    private static final Duration BASE_BACKOFF = Duration.ofMinutes(1);
    private static final int MAX_ATTEMPTS = 5;
    private static final Instant NOW = Instant.parse("2026-07-21T10:00:00Z");

    private static AlertDelivery queued() {
        return AlertDelivery.queue(
                MonitoringEventId.generate(), UserId.generate(), AlertChannel.SMS, NOW);
    }

    @Test
    @DisplayName("a queued delivery is pending and due immediately")
    void queuedIsDueNow() {
        var delivery = queued();

        assertThat(delivery.status()).isEqualTo(AlertDeliveryStatus.PENDING);
        assertThat(delivery.attemptCount()).isZero();
        assertThat(delivery.nextAttemptAt()).isEqualTo(NOW);
        assertThat(delivery.deliveredAt()).isEmpty();
    }

    @Test
    @DisplayName("claiming counts the attempt before the channel is contacted")
    void claimingCountsTheAttempt() {
        var delivery = queued();

        delivery.markInFlight(NOW);

        assertThat(delivery.status()).isEqualTo(AlertDeliveryStatus.IN_FLIGHT);
        assertThat(delivery.attemptCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("backoff doubles on each successive failure")
    void backoffDoubles() {
        var delivery = queued();

        delivery.markInFlight(NOW);
        delivery.recordFailure("gateway down", MAX_ATTEMPTS, BASE_BACKOFF, NOW);
        assertThat(delivery.nextAttemptAt()).isEqualTo(NOW.plus(Duration.ofMinutes(1)));

        delivery.markInFlight(NOW);
        delivery.recordFailure("gateway down", MAX_ATTEMPTS, BASE_BACKOFF, NOW);
        assertThat(delivery.nextAttemptAt()).isEqualTo(NOW.plus(Duration.ofMinutes(2)));

        delivery.markInFlight(NOW);
        delivery.recordFailure("gateway down", MAX_ATTEMPTS, BASE_BACKOFF, NOW);
        assertThat(delivery.nextAttemptAt()).isEqualTo(NOW.plus(Duration.ofMinutes(4)));

        assertThat(delivery.status()).isEqualTo(AlertDeliveryStatus.PENDING);
    }

    @Test
    @DisplayName("the delivery dead-letters on the last allowed attempt, not before")
    void deadLettersOnceBudgetIsSpent() {
        var delivery = queued();

        for (int attempt = 1; attempt < MAX_ATTEMPTS; attempt++) {
            delivery.markInFlight(NOW);
            delivery.recordFailure("gateway down", MAX_ATTEMPTS, BASE_BACKOFF, NOW);
            assertThat(delivery.status())
                    .as("attempt %d must still be retryable", attempt)
                    .isEqualTo(AlertDeliveryStatus.PENDING);
        }

        delivery.markInFlight(NOW);
        delivery.recordFailure("gateway down", MAX_ATTEMPTS, BASE_BACKOFF, NOW);

        assertThat(delivery.attemptCount()).isEqualTo(MAX_ATTEMPTS);
        assertThat(delivery.status()).isEqualTo(AlertDeliveryStatus.DEAD_LETTER);
        assertThat(delivery.isTerminal()).isTrue();
        assertThat(delivery.lastError()).contains("gateway down");
    }

    @Test
    @DisplayName("a delivered alert clears the last error and stamps the delivery time")
    void deliveredClearsError() {
        var delivery = queued();
        delivery.markInFlight(NOW);
        delivery.recordFailure("transient", MAX_ATTEMPTS, BASE_BACKOFF, NOW);

        delivery.markInFlight(NOW);
        delivery.markDelivered(NOW);

        assertThat(delivery.status()).isEqualTo(AlertDeliveryStatus.DELIVERED);
        assertThat(delivery.deliveredAt()).contains(NOW);
        assertThat(delivery.lastError()).isEmpty();
    }

    @Test
    @DisplayName("a terminal delivery can never be re-sent")
    void terminalDeliveryIsFrozen() {
        var delivery = queued();
        delivery.markInFlight(NOW);
        delivery.markDelivered(NOW);

        assertThatThrownBy(() -> delivery.markInFlight(NOW))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DELIVERED");
    }

    @Test
    @DisplayName("a delivery abandoned mid-dispatch returns to the queue")
    void staleClaimIsReleased() {
        var delivery = queued();
        delivery.markInFlight(NOW);

        var later = NOW.plus(Duration.ofMinutes(30));
        delivery.releaseStaleClaim(later);

        assertThat(delivery.status()).isEqualTo(AlertDeliveryStatus.PENDING);
        assertThat(delivery.nextAttemptAt()).isEqualTo(later);
        // The attempt is still counted: a worker that died after handing the
        // message to the carrier may well have sent it.
        assertThat(delivery.attemptCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("only an in-flight delivery can be released")
    void releaseRejectsPendingDelivery() {
        var delivery = queued();

        assertThatThrownBy(() -> delivery.releaseStaleClaim(NOW))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("an oversized error message is truncated to fit the column")
    void errorIsTruncated() {
        var delivery = queued();
        delivery.markInFlight(NOW);

        delivery.recordFailure("x".repeat(2_000), MAX_ATTEMPTS, BASE_BACKOFF, NOW);

        assertThat(delivery.lastError())
                .isPresent()
                .get()
                .asString()
                .hasSize(AlertDelivery.MAX_ERROR_LENGTH);
    }

    @Test
    @DisplayName("SMS outranks push and e-mail in the queue")
    void smsHasHighestPriority() {
        assertThat(AlertChannel.SMS.dispatchPriority())
                .isLessThan(AlertChannel.PUSH.dispatchPriority())
                .isLessThan(AlertChannel.EMAIL.dispatchPriority());
    }
}
