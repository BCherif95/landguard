package com.laboussole.domain.port.in;

import com.laboussole.domain.model.monitoring.MonitoringEventId;

public interface ResolveMonitoringEventUseCase {
    void execute(MonitoringEventId id);
}
