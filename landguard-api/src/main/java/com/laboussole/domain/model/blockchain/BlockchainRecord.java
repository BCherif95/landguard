package com.laboussole.domain.model.blockchain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One entry of the append-only ledger.
 *
 * <p>{@code chainIndex} gives the chain a strict total order — it cannot be
 * derived from {@code anchoredAt}, which ties whenever two anchors land in the
 * same microsecond and would then leave the chain head ambiguous.
 *
 * <p>{@code payloadHash} is {@code null} for records anchored before the
 * integrity migration: their link to the previous record is still checkable,
 * but their seal can never be recomputed. Verification reports those as
 * unsealed rather than as valid.
 */
public record BlockchainRecord(
        UUID id,
        long chainIndex,
        String entityType,
        String entityId,
        String hash,
        String previousHash,
        String payloadHash,
        Instant anchoredAt,
        String network,
        String transactionId
) {
    public BlockchainRecord {
        Objects.requireNonNull(id);
        Objects.requireNonNull(entityType);
        Objects.requireNonNull(entityId);
        Objects.requireNonNull(hash);
        Objects.requireNonNull(previousHash);
        Objects.requireNonNull(anchoredAt);
        Objects.requireNonNull(network);
        Objects.requireNonNull(transactionId);
        if (chainIndex < 0) {
            throw new IllegalArgumentException("chainIndex must be positive");
        }
    }

    /**
     * Seals a canonical payload at the given position of the chain, deriving
     * the payload digest, the chained hash and the transaction id from
     * {@link LedgerSeal}. This is the only supported way to mint a record: it
     * guarantees that a stored record always satisfies the seal formula.
     */
    public static BlockchainRecord seal(
            long chainIndex,
            String entityType,
            String entityId,
            String previousHash,
            String canonicalPayload,
            String network) {
        var payloadHash = LedgerSeal.payloadDigest(canonicalPayload);
        var hash = LedgerSeal.chainHash(previousHash, payloadHash);
        return new BlockchainRecord(
                UUID.randomUUID(),
                chainIndex,
                entityType,
                entityId,
                hash,
                previousHash,
                payloadHash,
                Instant.now(),
                network,
                LedgerSeal.transactionId(hash));
    }

    /** True when the seal can be recomputed, i.e. the payload digest is known. */
    public boolean isSealVerifiable() {
        return payloadHash != null && !payloadHash.isBlank();
    }
}
