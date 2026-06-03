package com.laboussole.domain.port.out;

import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.monitoring.MonitoringEventId;
import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import com.laboussole.domain.model.parcel.ParcelId;

import java.util.List;
import java.util.Optional;

public interface MonitoringRepository {
    void save(MonitoringEvent event);
    Optional<MonitoringEvent> findById(MonitoringEventId id);
    List<MonitoringEvent> findAll();
    List<MonitoringEvent> findByParcelId(ParcelId parcelId);
    List<MonitoringEvent> findBySeverity(MonitoringSeverity severity);
    void delete(MonitoringEventId id);
}
