package com.laboussole.application.service;

import com.laboussole.domain.model.User;
import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.heritage.Heir;
import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import com.laboussole.domain.model.notification.NotificationPreferences;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.port.out.AlertNotificationPort;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.NotificationPreferencesRepository;
import com.laboussole.domain.port.out.SuccessionRepository;
import com.laboussole.domain.port.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Routes HIGH/CRITICAL monitoring events to every alert channel each recipient
 * has enabled (push, e-mail, SMS).
 *
 * <p>Recipients are the parcel owner <em>and</em> every heir whose account is
 * linked to the parcel's succession plan (Feature 04.2: shared notifications —
 * all family members are alerted simultaneously, no member can act in secret).
 *
 * <p>Channels are dispatched in parallel on a dedicated executor and are
 * fault-isolated: a failing channel is logged and never prevents the others
 * from delivering (if the SMS gateway is down, the e-mail still goes out).
 */
@Service
public class MultiChannelAlertRoutingService {

    private static final Logger log = LoggerFactory.getLogger(MultiChannelAlertRoutingService.class);

    private static final Set<MonitoringSeverity> ALERTABLE_SEVERITIES =
            EnumSet.of(MonitoringSeverity.HIGH, MonitoringSeverity.CRITICAL);

    private final List<AlertNotificationPort> channels;
    private final LandParcelRepository parcelRepository;
    private final UserRepository userRepository;
    private final NotificationPreferencesRepository preferencesRepository;
    private final SuccessionRepository successionRepository;
    private final Executor alertExecutor;

    public MultiChannelAlertRoutingService(
            List<AlertNotificationPort> channels,
            LandParcelRepository parcelRepository,
            UserRepository userRepository,
            NotificationPreferencesRepository preferencesRepository,
            SuccessionRepository successionRepository,
            @Qualifier("alertTaskExecutor") Executor alertExecutor) {
        this.channels = List.copyOf(channels);
        this.parcelRepository = parcelRepository;
        this.userRepository = userRepository;
        this.preferencesRepository = preferencesRepository;
        this.successionRepository = successionRepository;
        this.alertExecutor = alertExecutor;
    }

    /**
     * Fires the enabled channels of every recipient for this event; returns a
     * future completing when every channel attempt has finished (success or
     * logged failure), so callers and tests can await without blocking the
     * detection flow.
     */
    public CompletableFuture<Void> route(MonitoringEvent event) {
        if (!ALERTABLE_SEVERITIES.contains(event.severity())) {
            return CompletableFuture.completedFuture(null);
        }

        var recipients = resolveRecipients(event);
        if (recipients.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        var dispatches = new ArrayList<CompletableFuture<Void>>();
        for (User recipient : recipients) {
            var preferences = preferencesRepository.findByUserId(recipient.id())
                    .orElseGet(() -> NotificationPreferences.defaults(recipient.id()));
            channels.stream()
                    .filter(channel -> preferences.isChannelEnabled(channel.channel()))
                    .forEach(channel -> dispatches.add(dispatchIsolated(channel, event, recipient)));
        }
        return CompletableFuture.allOf(dispatches.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<Void> dispatchIsolated(
            AlertNotificationPort channel, MonitoringEvent event, User recipient) {
        return CompletableFuture
                .runAsync(() -> channel.dispatch(event, recipient), alertExecutor)
                .exceptionally(failure -> {
                    log.error("Alert channel {} failed for event {} (parcel {}): {}",
                            channel.channel(), event.id().value(), event.parcelId().value(),
                            failure.getMessage(), failure);
                    return null;
                });
    }

    /** Owner first, then linked heirs, deduplicated while preserving order. */
    private List<User> resolveRecipients(MonitoringEvent event) {
        var parcel = parcelRepository.findById(event.parcelId()).orElse(null);
        if (parcel == null) {
            log.warn("Cannot route alert {}: parcel {} not found",
                    event.id().value(), event.parcelId().value());
            return List.of();
        }

        var recipientIds = new LinkedHashSet<UserId>();
        if (parcel.ownerUserId() != null) {
            recipientIds.add(parcel.ownerUserId());
        } else {
            log.warn("Alert {}: parcel {} has no owner user on record",
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
