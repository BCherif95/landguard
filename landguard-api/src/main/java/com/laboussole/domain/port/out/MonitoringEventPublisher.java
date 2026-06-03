package com.laboussole.domain.port.out;

import com.laboussole.domain.model.monitoring.MonitoringEvent;

public interface MonitoringEventPublisher {
    void publish(MonitoringEvent event);
}
