package com.laboussole.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SatelliteSnapshotSpringDataRepository extends JpaRepository<SatelliteSnapshotJpaEntity, UUID> {
    List<SatelliteSnapshotJpaEntity> findByParcelId(UUID parcelId);
}
