package com.laboussole.infrastructure.notification;

import com.laboussole.domain.model.User;
import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.notification.AlertChannel;
import com.laboussole.domain.port.out.AlertNotificationPort;
import com.laboussole.infrastructure.monitoring.SseMonitoringEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Push channel: broadcasts the alert on the existing SSE stream under the
 * dedicated {@code alert} event name. Reuses {@link SseMonitoringEventPublisher}
 * rather than maintaining a second emitter registry.
 */
@Component
class PushAlertAdapter implements AlertNotificationPort {

    private final SseMonitoringEventPublisher ssePublisher;

    PushAlertAdapter(SseMonitoringEventPublisher ssePublisher) {
        this.ssePublisher = ssePublisher;
    }

    @Override
    public AlertChannel channel() {
        return AlertChannel.PUSH;
    }

    @Override
    public void dispatch(MonitoringEvent event, User recipient) {
        ssePublisher.publishAlert(event);
    }
}
