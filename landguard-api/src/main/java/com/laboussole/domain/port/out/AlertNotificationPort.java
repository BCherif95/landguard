package com.laboussole.domain.port.out;

import com.laboussole.domain.model.User;
import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.notification.AlertChannel;

/**
 * Driven port: one alert-delivery channel (push, e-mail, SMS…).
 *
 * <p>Implementations may throw on delivery failure — the routing
 * orchestrator isolates each channel so one failing adapter never
 * prevents the others from dispatching.
 */
public interface AlertNotificationPort {

    /** Which user preference toggle governs this channel. */
    AlertChannel channel();

    void dispatch(MonitoringEvent event, User recipient);
}
