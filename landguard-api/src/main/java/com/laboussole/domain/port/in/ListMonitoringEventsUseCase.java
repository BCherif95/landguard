package com.laboussole.domain.port.in;

import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import com.laboussole.domain.model.parcel.ParcelId;

import java.util.List;

public interface ListMonitoringEventsUseCase {
    List<MonitoringEvent> execute(Query query);

    record Query(
            ParcelId parcelId,
            MonitoringSeverity severity
    ) {}
}
