package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MonitoringEventSpringDataRepository extends JpaRepository<MonitoringEventJpaEntity, UUID> {
    List<MonitoringEventJpaEntity> findByParcelId(UUID parcelId);
    List<MonitoringEventJpaEntity> findBySeverity(MonitoringSeverity severity);
}
