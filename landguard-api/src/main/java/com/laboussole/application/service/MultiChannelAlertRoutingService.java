package com.laboussole.application.service;

import com.laboussole.domain.model.User;
import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import com.laboussole.domain.model.notification.NotificationPreferences;
import com.laboussole.domain.port.out.AlertNotificationPort;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.NotificationPreferencesRepository;
import com.laboussole.domain.port.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Routes HIGH/CRITICAL monitoring events to every alert channel the parcel
 * owner has enabled (push, e-mail, SMS).
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
    private final Executor alertExecutor;

    public MultiChannelAlertRoutingService(
            List<AlertNotificationPort> channels,
            LandParcelRepository parcelRepository,
            UserRepository userRepository,
            NotificationPreferencesRepository preferencesRepository,
            @Qualifier("alertTaskExecutor") Executor alertExecutor) {
        this.channels = List.copyOf(channels);
        this.parcelRepository = parcelRepository;
        this.userRepository = userRepository;
        this.preferencesRepository = preferencesRepository;
        this.alertExecutor = alertExecutor;
    }

    /**
     * Fires the enabled channels for this event; returns a future completing
     * when every channel attempt has finished (success or logged failure),
     * so callers and tests can await without blocking the detection flow.
     */
    public CompletableFuture<Void> route(MonitoringEvent event) {
        if (!ALERTABLE_SEVERITIES.contains(event.severity())) {
            return CompletableFuture.completedFuture(null);
        }

        var owner = resolveOwner(event);
        if (owner == null) {
            return CompletableFuture.completedFuture(null);
        }

        var preferences = preferencesRepository.findByUserId(owner.id())
                .orElseGet(() -> NotificationPreferences.defaults(owner.id()));

        var dispatches = channels.stream()
                .filter(channel -> preferences.isChannelEnabled(channel.channel()))
                .map(channel -> dispatchIsolated(channel, event, owner))
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(dispatches);
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

    private User resolveOwner(MonitoringEvent event) {
        var parcel = parcelRepository.findById(event.parcelId()).orElse(null);
        if (parcel == null) {
            log.warn("Cannot route alert {}: parcel {} not found",
                    event.id().value(), event.parcelId().value());
            return null;
        }
        if (parcel.ownerUserId() == null) {
            log.warn("Cannot route alert {}: parcel {} has no owner user on record",
                    event.id().value(), parcel.reference().value());
            return null;
        }
        var owner = userRepository.findById(parcel.ownerUserId()).orElse(null);
        if (owner == null) {
            log.warn("Cannot route alert {}: owner user {} not found",
                    event.id().value(), parcel.ownerUserId().value());
        }
        return owner;
    }
}
