package com.laboussole.infrastructure.blockchain;

import com.laboussole.domain.model.blockchain.BlockchainRecord;
import com.laboussole.domain.model.blockchain.LedgerSeal;
import com.laboussole.domain.port.out.BlockchainService;
import com.laboussole.domain.port.out.blockchain.BlockchainRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Local append-only hash chain backed by the {@code blockchain_records} table.
 *
 * <p>Each anchor is a real SHA-256 seal chained to its predecessor (see
 * {@link LedgerSeal}), so altering any stored record invalidates every
 * subsequent hash — the integrity property the PRD requires ("toute
 * modification rompt la chaîne de validation") — without depending on an
 * external blockchain network.
 *
 * <p><strong>Concurrency.</strong> Appending is serialised by taking a write
 * lock on the current chain head, so two simultaneous anchors cannot read the
 * same parent hash and fork the ledger. The unique indexes on
 * {@code chain_index} and {@code previous_hash} are the structural backstop:
 * they also cover the one case the lock cannot, a race on the very first
 * (genesis) record, where there is no row to lock yet. A loser in that race
 * fails its transaction on a constraint violation and must retry — deliberately
 * not swallowed here, since a silently dropped anchor would leave a succession
 * plan believing it was sealed.
 */
@Service
@Slf4j
public class HashChainLedgerAdapter implements BlockchainService {

    static final String NETWORK = "LANDGUARD_LOCAL_LEDGER";

    private final BlockchainRecordRepository records;

    public HashChainLedgerAdapter(BlockchainRecordRepository records) {
        this.records = records;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public BlockchainRecord anchor(String entityType, String entityId, String canonicalPayload) {
        var head = records.lockChainHead();
        long chainIndex = head.map(record -> record.chainIndex() + 1).orElse(0L);
        String previousHash = head.map(BlockchainRecord::hash).orElse(LedgerSeal.GENESIS_PREVIOUS_HASH);

        var record = BlockchainRecord.seal(
                chainIndex, entityType, entityId, previousHash, canonicalPayload, NETWORK);
        records.save(record);

        log.info("Anchored {} {} at chain index {}: {}",
                entityType, entityId, record.chainIndex(), record.hash());
        return record;
    }
}
