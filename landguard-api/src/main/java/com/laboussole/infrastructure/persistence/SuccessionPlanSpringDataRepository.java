package com.laboussole.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SuccessionPlanSpringDataRepository extends JpaRepository<SuccessionPlanJpaEntity, UUID> {
    Optional<SuccessionPlanJpaEntity> findByParcelId(UUID parcelId);
}
