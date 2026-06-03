package com.laboussole.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TitleVerificationSpringDataRepository extends JpaRepository<TitleVerificationCaseJpaEntity, UUID> {
    Optional<TitleVerificationCaseJpaEntity> findByParcelId(UUID parcelId);
    Optional<TitleVerificationCaseJpaEntity> findByCaseReference(String caseReference);
}
