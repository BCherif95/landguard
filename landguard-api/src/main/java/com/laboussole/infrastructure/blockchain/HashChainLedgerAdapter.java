package com.laboussole.infrastructure.blockchain;

import com.laboussole.domain.model.blockchain.BlockchainRecord;
import com.laboussole.domain.port.out.BlockchainService;
import com.laboussole.domain.port.out.blockchain.BlockchainRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Local append-only hash chain backed by the {@code blockchain_records} table.
 *
 * <p>Each anchor is a real SHA-256 seal: {@code hash = SHA-256(previousHash || payload)}.
 * The previous entry's hash is embedded in the next one, so altering any stored
 * record invalidates every subsequent hash — the integrity property the PRD
 * requires ("toute modification rompt la chaîne de validation"), without
 * depending on an external blockchain network.
 */
@Service
@Slf4j
public class HashChainLedgerAdapter implements BlockchainService {

    static final String NETWORK = "LANDGUARD_LOCAL_LEDGER";
    private static final String GENESIS_PREVIOUS_HASH = "0x" + "0".repeat(64);

    private final BlockchainRecordRepository records;

    public HashChainLedgerAdapter(BlockchainRecordRepository records) {
        this.records = records;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public BlockchainRecord anchor(String entityType, String entityId, String canonicalPayload) {
        var previousHash = records.findMostRecent(1).stream()
                .findFirst()
                .map(BlockchainRecord::hash)
                .orElse(GENESIS_PREVIOUS_HASH);

        var hash = "0x" + sha256Hex(previousHash + "\n" + canonicalPayload);
        // The transaction id is derived from the hash: deterministic and verifiable.
        var transactionId = "lg-" + hash.substring(2, 18);

        var record = BlockchainRecord.create(
                entityType, entityId, hash, previousHash, NETWORK, transactionId);
        records.save(record);
        log.info("Anchored {} {} on local ledger: {}", entityType, entityId, hash);
        return record;
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
