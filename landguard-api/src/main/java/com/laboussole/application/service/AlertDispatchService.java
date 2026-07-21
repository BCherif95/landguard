package com.laboussole.application.service;

import com.laboussole.domain.model.User;
import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.notification.AlertChannel;
import com.laboussole.domain.model.notification.AlertDelivery;
import com.laboussole.domain.port.out.AlertDeliveryRepository;
import com.laboussole.domain.port.out.AlertNotificationPort;
import com.laboussole.domain.port.out.MonitoringRepository;
import com.laboussole.domain.port.out.UserRepository;
import com.laboussole.infrastructure.notification.AlertDispatchProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Drains the durable alert queue: claims what is due, sends it, and records the
 * outcome of every attempt.
 *
 * <p>Ordering is by channel priority, so an SMS to an owner standing on their
 * land is never stuck behind a backlog of diaspora e-mails.
 *
 * <p>Each delivery is dispatched outside the claiming transaction and its
 * outcome is written in its own transaction, so one hung SMTP connection
 * cannot roll back or block the deliveries around it.
 */
@Service
public class AlertDispatchService {

    private static final Logger log = LoggerFactory.getLogger(AlertDispatchService.class);

    private final Map<AlertChannel, AlertNotificationPort> channels = new EnumMap<>(AlertChannel.class);
    private final AlertDeliveryRepository deliveries;
    private final MonitoringRepository monitoringRepository;
    private final UserRepository userRepository;
    private final AlertDispatchProperties properties;

    public AlertDispatchService(
            List<AlertNotificationPort> channelAdapters,
            AlertDeliveryRepository deliveries,
            MonitoringRepository monitoringRepository,
            UserRepository userRepository,
            AlertDispatchProperties properties) {
        channelAdapters.forEach(adapter -> this.channels.put(adapter.channel(), adapter));
        this.deliveries = deliveries;
        this.monitoringRepository = monitoringRepository;
        this.userRepository = userRepository;
        this.properties = properties;
    }

    /**
     * Runs one drain cycle.
     *
     * @return the number of deliveries attempted
     */
    public int drain() {
        requeueStaleClaims();

        var claimed = deliveries.claimDueForDispatch(properties.batchSize(), Instant.now());
        if (claimed.isEmpty()) {
            return 0;
        }

        log.debug("Dispatching {} claimed alert delivery(ies)", claimed.size());
        for (AlertDelivery delivery : claimed) {
            attempt(delivery);
        }
        return claimed.size();
    }

    /**
     * Sends one delivery and records the outcome. Never throws: a delivery that
     * blows up must not abort the rest of the batch.
     *
     * <p>No explicit transaction here on purpose. An attempt writes the ledger
     * exactly once — either {@code markDelivered} or {@code recordFailure} —
     * and that single save is already atomic. Wrapping the loop in one
     * transaction would instead let a late failure roll back the outcomes of
     * deliveries that already went out over the wire.
     */
    private void attempt(AlertDelivery delivery) {
        try {
            var adapter = channels.get(delivery.channel());
            if (adapter == null) {
                // Channel disabled or removed since the alert was queued.
                fail(delivery, "Aucun adaptateur disponible pour le canal " + delivery.channel());
                return;
            }

            MonitoringEvent event = monitoringRepository.findById(delivery.eventId()).orElse(null);
            if (event == null) {
                fail(delivery, "Événement de surveillance " + delivery.eventId().value() + " introuvable");
                return;
            }
            User recipient = userRepository.findById(delivery.recipientId()).orElse(null);
            if (recipient == null) {
                fail(delivery, "Destinataire " + delivery.recipientId().value() + " introuvable");
                return;
            }

            adapter.dispatch(event, recipient);
            delivery.markDelivered(Instant.now());
            deliveries.save(delivery);
            log.info("Alert {} delivered over {} to user {} (attempt {})",
                    delivery.id().value(), delivery.channel(),
                    delivery.recipientId().value(), delivery.attemptCount());

        } catch (Exception failure) {
            fail(delivery, failure.getMessage() == null
                    ? failure.getClass().getSimpleName()
                    : failure.getMessage());
        }
    }

    private void fail(AlertDelivery delivery, String reason) {
        delivery.recordFailure(reason, properties.maxAttempts(), properties.baseBackoff(), Instant.now());
        deliveries.save(delivery);
        if (delivery.isTerminal()) {
            log.error("Alert {} DEAD-LETTERED after {} attempt(s) over {} to user {}: {}",
                    delivery.id().value(), delivery.attemptCount(), delivery.channel(),
                    delivery.recipientId().value(), reason);
        } else {
            log.warn("Alert {} failed over {} (attempt {}), retry at {}: {}",
                    delivery.id().value(), delivery.channel(), delivery.attemptCount(),
                    delivery.nextAttemptAt(), reason);
        }
    }

    /** Returns deliveries abandoned by a dead worker to the pending queue. */
    private void requeueStaleClaims() {
        var staleBefore = Instant.now().minus(properties.staleAfter());
        var stale = deliveries.findStaleInFlight(staleBefore, properties.batchSize());
        if (stale.isEmpty()) {
            return;
        }
        var now = Instant.now();
        stale.forEach(delivery -> {
            delivery.releaseStaleClaim(now);
            deliveries.save(delivery);
        });
        log.warn("Requeued {} alert delivery(ies) abandoned mid-dispatch", stale.size());
    }
}
