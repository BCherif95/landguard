package com.laboussole.domain.model.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * The cryptographic rule that binds the ledger together, framework-free so the
 * writing adapter and the verification use case apply strictly the same
 * formula.
 *
 * <p>Sealing a record is a two-step operation:
 * <pre>
 *   payloadHash = SHA-256(canonicalPayload)
 *   hash        = "0x" + SHA-256(previousHash + "\n" + payloadHash)
 * </pre>
 *
 * <p>Chaining over the payload <em>digest</em> rather than over the payload
 * itself is what makes the ledger verifiable from its own rows: {@code hash}
 * can be recomputed from two stored columns, so an auditor can replay the whole
 * chain without the platform having to keep a second copy of every succession
 * plan — personal data included — inside an append-only table.
 */
public final class LedgerSeal {

    /** Parent hash of the very first record; no record precedes it. */
    public static final String GENESIS_PREVIOUS_HASH = "0x" + "0".repeat(64);

    /** Length of a "0x"-prefixed SHA-256 hex digest. */
    public static final int HASH_LENGTH = 66;

    private LedgerSeal() {
    }

    /** Bare (un-prefixed) SHA-256 digest of the canonical payload. */
    public static String payloadDigest(String canonicalPayload) {
        return sha256Hex(canonicalPayload);
    }

    /** The chained seal of a record, given its parent hash and payload digest. */
    public static String chainHash(String previousHash, String payloadHash) {
        return "0x" + sha256Hex(previousHash + "\n" + payloadHash);
    }

    /**
     * Ledger transaction reference, derived from the seal so it stays
     * deterministic and independently recomputable.
     */
    public static String transactionId(String hash) {
        return "lg-" + hash.substring(2, 18);
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
