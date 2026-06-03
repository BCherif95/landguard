package com.laboussole.infrastructure.persistence;

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

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String hash;

    @Column(name = "previous_hash", columnDefinition = "TEXT")
    private String previousHash;

    @Column(name = "anchored_at", nullable = false)
    private Instant anchoredAt;

    @Column(nullable = false)
    private String network;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;
}
