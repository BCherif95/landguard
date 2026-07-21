package com.laboussole.domain.model.blockchain;

import java.util.UUID;

/**
 * A single verification failure, located precisely enough for an operator — or
 * a court-appointed expert — to inspect the offending row.
 *
 * @param chainIndex position in the chain where the break was observed
 * @param recordId   identifier of the record that failed, when one exists
 * @param type       nature of the break
 * @param detail     French, human-readable explanation, displayed as-is
 */
public record LedgerBreak(
        long chainIndex,
        UUID recordId,
        LedgerBreakType type,
        String detail
) {
}
