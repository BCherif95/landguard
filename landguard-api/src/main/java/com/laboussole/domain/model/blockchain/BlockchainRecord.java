package com.laboussole.domain.model.blockchain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record BlockchainRecord(
        UUID id,
        String entityType,
        String entityId,
        String hash,
        String previousHash,
        Instant anchoredAt,
        String network,
        String transactionId
) {
    public BlockchainRecord {
        Objects.requireNonNull(id);
        Objects.requireNonNull(entityType);
        Objects.requireNonNull(entityId);
        Objects.requireNonNull(hash);
        Objects.requireNonNull(anchoredAt);
        Objects.requireNonNull(network);
        Objects.requireNonNull(transactionId);
    }

    public static BlockchainRecord create(String entityType, String entityId, String hash, String previousHash, String network, String transactionId) {
        return new BlockchainRecord(UUID.randomUUID(), entityType, entityId, hash, previousHash, Instant.now(), network, transactionId);
    }
}
