package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.blockchain.LedgerSeal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blockchain_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainRecordJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    /**
     * Strict position in the chain. Unique in database, which is what makes a
     * fork structurally impossible even when two anchors race.
     */
    @Column(name = "chain_index", nullable = false, updatable = false)
    private long chainIndex;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(nullable = false, updatable = false, length = LedgerSeal.HASH_LENGTH)
    private String hash;

    @Column(name = "previous_hash", nullable = false, updatable = false, length = LedgerSeal.HASH_LENGTH)
    private String previousHash;

    /** SHA-256 of the canonical payload; null for pre-migration rows. */
    @Column(name = "payload_hash", updatable = false, length = 64)
    private String payloadHash;

    @Column(name = "anchored_at", nullable = false)
    private Instant anchoredAt;

    @Column(nullable = false)
    private String network;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;
}
