package com.laboussole.interfaces.rest.blockchain;

import com.laboussole.domain.model.blockchain.BlockchainRecord;

import java.time.Instant;
import java.util.UUID;

public record BlockchainRecordResponse(
        UUID id,
        String entityType,
        String entityId,
        String hash,
        String previousHash,
        Instant anchoredAt,
        String network,
        String transactionId
) {
    public static BlockchainRecordResponse from(BlockchainRecord record) {
        return new BlockchainRecordResponse(
                record.id(),
                record.entityType(),
                record.entityId(),
                record.hash(),
                record.previousHash(),
                record.anchoredAt(),
                record.network(),
                record.transactionId()
        );
    }
}
