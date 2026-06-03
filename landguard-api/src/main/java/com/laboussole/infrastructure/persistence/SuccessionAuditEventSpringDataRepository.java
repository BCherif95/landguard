package com.laboussole.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SuccessionAuditEventSpringDataRepository extends JpaRepository<SuccessionAuditEventJpaEntity, UUID> {
    List<SuccessionAuditEventJpaEntity> findBySuccessionPlanId(UUID successionPlanId);
}
