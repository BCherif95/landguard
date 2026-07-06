package com.laboussole.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface OcrExtractionSpringDataRepository extends JpaRepository<OcrExtractionJpaEntity, UUID> {

    Optional<OcrExtractionJpaEntity> findByStorageKey(String storageKey);
}
