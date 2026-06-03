package com.laboussole.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LegalDisputeSpringDataRepository extends JpaRepository<LegalDisputeJpaEntity, UUID> {
    List<LegalDisputeJpaEntity> findByParcelId(UUID parcelId);
}
