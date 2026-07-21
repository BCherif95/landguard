package com.laboussole.domain.port.out.blockchain;

import com.laboussole.domain.model.blockchain.BlockchainRecord;

import java.util.List;
import java.util.Optional;

public interface BlockchainRecordRepository {
    void save(BlockchainRecord record);
    List<BlockchainRecord> findByEntityId(String entityId);
    Optional<BlockchainRecord> findByHash(String hash);

    /** Most recent anchors first. */
    List<BlockchainRecord> findMostRecent(int limit);

    /**
     * Returns the highest-indexed record, holding a write lock on it until the
     * calling transaction commits, so concurrent anchors cannot read the same
     * chain head and fork the ledger. Empty when nothing is anchored yet.
     */
    Optional<BlockchainRecord> lockChainHead();

    /**
     * A page of the chain in ascending index order, starting at
     * {@code fromChainIndex} inclusive. Used to replay the ledger without
     * loading an ever-growing table into memory.
     */
    List<BlockchainRecord> findChainSlice(long fromChainIndex, int limit);
}
