package com.laboussole.domain.model.blockchain;

/** Why a ledger record failed verification. */
public enum LedgerBreakType {

    /**
     * The record's hash is not the seal of its own stored parent hash and
     * payload digest: the row was edited after being anchored.
     */
    SEAL_MISMATCH,

    /**
     * The record does not point at the hash of the record that precedes it:
     * a row was inserted, removed or re-parented.
     */
    BROKEN_LINK,

    /**
     * The chain index jumps: at least one record was deleted from the middle
     * of the ledger.
     */
    INDEX_GAP
}
