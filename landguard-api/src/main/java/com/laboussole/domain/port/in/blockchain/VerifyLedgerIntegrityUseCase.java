package com.laboussole.domain.port.in.blockchain;

import com.laboussole.domain.model.blockchain.LedgerIntegrityReport;

/**
 * Replays the whole ledger and reports whether every seal still holds.
 *
 * <p>This is what makes an anchored certificate defensible: an expert can ask
 * the platform to prove, on demand, that no historical record was altered.
 */
public interface VerifyLedgerIntegrityUseCase {

    LedgerIntegrityReport execute();
}
