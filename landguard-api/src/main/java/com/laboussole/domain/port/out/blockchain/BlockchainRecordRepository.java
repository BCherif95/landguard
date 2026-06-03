package com.laboussole.domain.port.out.blockchain;

import com.laboussole.domain.model.blockchain.BlockchainRecord;

import java.util.List;
import java.util.Optional;

public interface BlockchainRecordRepository {
    void save(BlockchainRecord record);
    List<BlockchainRecord> findByEntityId(String entityId);
    Optional<BlockchainRecord> findByHash(String hash);
}
