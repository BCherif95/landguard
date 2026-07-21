package com.laboussole.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlockchainRecordSpringDataRepository extends JpaRepository<BlockchainRecordJpaEntity, UUID> {

    List<BlockchainRecordJpaEntity> findByEntityId(String entityId);

    Optional<BlockchainRecordJpaEntity> findByHash(String hash);

    /**
     * Highest-indexed record, locked for write until the caller's transaction
     * commits (SELECT … FOR UPDATE). Serialising anchors on the chain head is
     * what stops two concurrent writers from reading the same parent hash.
     *
     * <p>When the table is empty there is no row to lock; the unique index on
     * {@code chain_index} is the backstop that rejects a duplicate genesis.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r FROM BlockchainRecordJpaEntity r
            WHERE r.chainIndex = (SELECT MAX(r2.chainIndex) FROM BlockchainRecordJpaEntity r2)
            """)
    Optional<BlockchainRecordJpaEntity> lockChainHead();

    List<BlockchainRecordJpaEntity> findByChainIndexGreaterThanEqualOrderByChainIndexAsc(
            long fromChainIndex, Pageable pageable);
}
