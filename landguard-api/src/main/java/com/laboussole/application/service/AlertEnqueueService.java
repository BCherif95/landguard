package com.laboussole.application.service;

import com.laboussole.domain.model.User;
import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.heritage.Heir;
import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import com.laboussole.domain.model.notification.AlertChannel;
import com.laboussole.domain.model.notification.AlertDelivery;
import com.laboussole.domain.model.notification.NotificationPreferences;
import com.laboussole.domain.port.out.AlertDeliveryRepository;
import com.laboussole.domain.port.out.AlertNotificationPort;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.NotificationPreferencesRepository;
import com.laboussole.domain.port.out.SuccessionRepository;
import com.laboussole.domain.port.out.UserRepository;
import com.laboussole.domain.model.parcel.LandParcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Turns a HIGH/CRITICAL monitoring event into durable delivery intents — one
 * per recipient and enabled channel — written in the caller's transaction.
 *
 * <p>Recipients are the parcel owner <em>and</em> every heir whose account is
 * linked to the parcel's succession plan (Feature 04.2: shared notifications —
 * all family members are alerted simultaneously, no member can act in secret).
 *
 * <p>Nothing is sent here. Enqueueing inside the event's transaction is what
 * makes the alert atomic with the fact that justifies it: if the transaction
 * rolls back, no phantom SMS goes out; if the process dies right after commit,
 * the queued rows are still on disk and the dispatcher picks them up.
 */
@Service
public class AlertEnqueueService {

    private static final Logger log = LoggerFactory.getLogger(AlertEnqueueService.class);

    private static final Set<MonitoringSeverity> ALERTABLE_SEVERITIES =
            EnumSet.of(MonitoringSeverity.HIGH, MonitoringSeverity.CRITICAL);

    private final List<AlertChannel> availableChannels;
    private final AlertDeliveryRepository deliveries;
    private final LandParcelRepository parcelRepository;
    private final UserRepository userRepository;
    private final NotificationPreferencesRepository preferencesRepository;
    private final SuccessionRepository successionRepository;

    public AlertEnqueueService(
            List<AlertNotificationPort> channels,
            AlertDeliveryRepository deliveries,
            LandParcelRepository parcelRepository,
            UserRepository userRepository,
            NotificationPreferencesRepository preferencesRepository,
            SuccessionRepository successionRepository) {
        this.availableChannels = channels.stream()
                .map(AlertNotificationPort::channel)
                .sorted(Comparator.comparingInt(AlertChannel::dispatchPriority))
                .toList();
        this.deliveries = deliveries;
        this.parcelRepository = parcelRepository;
        this.userRepository = userRepository;
        this.preferencesRepository = preferencesRepository;
        this.successionRepository = successionRepository;
    }

    /**
     * Queues every delivery this event warrants.
     *
     * @return the number of deliveries queued
     */
    public int enqueue(MonitoringEvent event) {
        if (!ALERTABLE_SEVERITIES.contains(event.severity())) {
            return 0;
        }

        var recipients = resolveRecipients(event);
        if (recipients.isEmpty()) {
            return 0;
        }

        var now = Instant.now();
        var queued = new ArrayList<AlertDelivery>();
        for (User recipient : recipients) {
            var preferences = preferencesRepository.findByUserId(recipient.id())
                    .orElseGet(() -> NotificationPreferences.defaults(recipient.id()));
            for (AlertChannel channel : availableChannels) {
                if (preferences.isChannelEnabled(channel)) {
                    queued.add(AlertDelivery.queue(event.id(), recipient.id(), channel, now));
                }
            }
        }

        if (queued.isEmpty()) {
            log.warn("Event {} has {} recipient(s) but no enabled channel — nothing queued",
                    event.id().value(), recipients.size());
            return 0;
        }

        deliveries.saveAll(queued);
        log.info("Queued {} alert delivery(ies) for event {} (severity {})",
                queued.size(), event.id().value(), event.severity());
        return queued.size();
    }

    /** Owner first, then linked heirs, deduplicated while preserving order. */
    private List<User> resolveRecipients(MonitoringEvent event) {
        var parcel = parcelRepository.findById(event.parcelId()).orElse(null);
        if (parcel == null) {
            log.warn("Cannot queue alerts for {}: parcel {} not found",
                    event.id().value(), event.parcelId().value());
            return List.of();
        }

        var recipientIds = new LinkedHashSet<UserId>();
        if (parcel.ownerUserId() != null) {
            recipientIds.add(parcel.ownerUserId());
        } else {
            log.warn("Event {}: parcel {} has no owner user on record",
                    event.id().value(), parcel.reference().value());
        }
        recipientIds.addAll(linkedHeirIds(parcel));

        return recipientIds.stream()
                .map(id -> userRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    private List<UserId> linkedHeirIds(LandParcel parcel) {
        return successionRepository.findByParcelId(parcel.id())
                .map(plan -> plan.heirs().stream()
                        .map(Heir::linkedUserId)
                        .filter(Objects::nonNull)
                        .toList())
                .orElse(List.of());
    }
}
