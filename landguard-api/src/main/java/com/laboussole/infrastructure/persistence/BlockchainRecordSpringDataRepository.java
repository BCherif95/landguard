package com.laboussole.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlockchainRecordSpringDataRepository extends JpaRepository<BlockchainRecordJpaEntity, UUID> {
    List<BlockchainRecordJpaEntity> findByEntityId(String entityId);
    Optional<BlockchainRecordJpaEntity> findByHash(String hash);
}
