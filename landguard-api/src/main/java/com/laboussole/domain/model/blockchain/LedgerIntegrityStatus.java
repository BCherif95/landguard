package com.laboussole.domain.model.blockchain;

/** Overall verdict of a full ledger replay. */
public enum LedgerIntegrityStatus {

    /** Nothing has been anchored yet. */
    EMPTY,

    /** Every record was replayed and every seal matches. */
    INTACT,

    /**
     * No break was found, but some records predate the integrity migration and
     * carry no payload digest, so their seal could not be recomputed. The chain
     * links are verified; the seals of those records are taken on trust.
     */
    PARTIALLY_VERIFIABLE,

    /** At least one record failed verification: the ledger is compromised. */
    BROKEN
}
