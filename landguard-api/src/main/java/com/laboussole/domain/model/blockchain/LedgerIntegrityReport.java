package com.laboussole.domain.model.blockchain;

import java.util.List;

/**
 * Outcome of replaying the whole ledger.
 *
 * <p>This is the artefact that turns the hash chain from a claim into a proof:
 * without it the platform stores seals nobody can check, and the "any
 * modification breaks the chain" property is unenforceable.
 *
 * @param status          overall verdict
 * @param recordsChecked  number of records replayed
 * @param sealedRecords   records whose seal was recomputed and matched
 * @param unsealedRecords records with no payload digest (pre-migration rows)
 * @param breaks          every failure found, in chain order
 */
public record LedgerIntegrityReport(
        LedgerIntegrityStatus status,
        long recordsChecked,
        long sealedRecords,
        long unsealedRecords,
        List<LedgerBreak> breaks
) {
    public LedgerIntegrityReport {
        breaks = List.copyOf(breaks);
    }

    public static LedgerIntegrityReport empty() {
        return new LedgerIntegrityReport(LedgerIntegrityStatus.EMPTY, 0, 0, 0, List.of());
    }

    /**
     * Builds the verdict from the counters gathered during the replay: any
     * break makes the ledger BROKEN, otherwise unsealed legacy rows downgrade
     * the verdict from INTACT to PARTIALLY_VERIFIABLE.
     */
    public static LedgerIntegrityReport of(
            long recordsChecked, long sealedRecords, long unsealedRecords, List<LedgerBreak> breaks) {
        if (recordsChecked == 0) {
            return empty();
        }
        LedgerIntegrityStatus status;
        if (!breaks.isEmpty()) {
            status = LedgerIntegrityStatus.BROKEN;
        } else if (unsealedRecords > 0) {
            status = LedgerIntegrityStatus.PARTIALLY_VERIFIABLE;
        } else {
            status = LedgerIntegrityStatus.INTACT;
        }
        return new LedgerIntegrityReport(status, recordsChecked, sealedRecords, unsealedRecords, breaks);
    }

    public boolean isCompromised() {
        return status == LedgerIntegrityStatus.BROKEN;
    }
}
