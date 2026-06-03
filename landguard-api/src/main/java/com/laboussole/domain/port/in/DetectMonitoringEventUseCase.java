package com.laboussole.domain.port.in;

import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.monitoring.MonitoringEventType;
import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import com.laboussole.domain.model.monitoring.MonitoringSource;
import com.laboussole.domain.model.parcel.ParcelGeometry;
import com.laboussole.domain.model.parcel.ParcelId;

public interface DetectMonitoringEventUseCase {
    MonitoringEvent execute(Command command);

    record Command(
            ParcelId parcelId,
            MonitoringEventType type,
            MonitoringSeverity severity,
            int confidenceScore,
            ParcelGeometry.Coordinate coordinates,
            String description,
            MonitoringSource source,
            String imageUrl
    ) {}
}
