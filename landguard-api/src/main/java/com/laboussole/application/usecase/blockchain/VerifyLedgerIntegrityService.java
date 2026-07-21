package com.laboussole.application.usecase.blockchain;

import com.laboussole.domain.model.blockchain.BlockchainRecord;
import com.laboussole.domain.model.blockchain.LedgerBreak;
import com.laboussole.domain.model.blockchain.LedgerBreakType;
import com.laboussole.domain.model.blockchain.LedgerIntegrityReport;
import com.laboussole.domain.model.blockchain.LedgerSeal;
import com.laboussole.domain.port.in.blockchain.VerifyLedgerIntegrityUseCase;
import com.laboussole.domain.port.out.blockchain.BlockchainRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Replays the ledger from genesis and checks three invariants on every record:
 * contiguous chain index, parent hash equal to the previous record's hash, and
 * a seal that recomputes to the stored hash.
 *
 * <p>The walk is paged so the ledger can grow indefinitely without the
 * verification loading it whole; the running hash simply carries across page
 * boundaries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VerifyLedgerIntegrityService implements VerifyLedgerIntegrityUseCase {

    /** Rows fetched per round-trip while walking the chain. */
    static final int PAGE_SIZE = 500;

    private final BlockchainRecordRepository records;

    @Override
    @Transactional(readOnly = true)
    public LedgerIntegrityReport execute() {
        var breaks = new ArrayList<LedgerBreak>();
        long checked = 0;
        long sealed = 0;
        long unsealed = 0;

        long nextIndex = 0;
        String expectedPreviousHash = LedgerSeal.GENESIS_PREVIOUS_HASH;

        List<BlockchainRecord> page;
        while (!(page = records.findChainSlice(nextIndex, PAGE_SIZE)).isEmpty()) {
            for (BlockchainRecord record : page) {
                checked++;

                if (record.chainIndex() != nextIndex) {
                    breaks.add(new LedgerBreak(
                            record.chainIndex(),
                            record.id(),
                            LedgerBreakType.INDEX_GAP,
                            "Rupture de séquence : l'entrée n° %d succède à la position %d attendue."
                                    .formatted(record.chainIndex(), nextIndex)));
                }

                if (!expectedPreviousHash.equals(record.previousHash())) {
                    breaks.add(new LedgerBreak(
                            record.chainIndex(),
                            record.id(),
                            LedgerBreakType.BROKEN_LINK,
                            ("Chaînage rompu : l'entrée n° %d référence une empreinte parente qui ne"
                                    + " correspond pas à l'entrée précédente.")
                                    .formatted(record.chainIndex())));
                }

                if (record.isSealVerifiable()) {
                    var recomputed = LedgerSeal.chainHash(record.previousHash(), record.payloadHash());
                    if (!recomputed.equals(record.hash())) {
                        breaks.add(new LedgerBreak(
                                record.chainIndex(),
                                record.id(),
                                LedgerBreakType.SEAL_MISMATCH,
                                "Sceau invalide : l'entrée n° %d a été modifiée après son ancrage."
                                        .formatted(record.chainIndex())));
                    } else {
                        sealed++;
                    }
                } else {
                    unsealed++;
                }

                expectedPreviousHash = record.hash();
                nextIndex = record.chainIndex() + 1;
            }
        }

        var report = LedgerIntegrityReport.of(checked, sealed, unsealed, breaks);
        if (report.isCompromised()) {
            log.error("Ledger integrity check FAILED: {} record(s) checked, {} break(s) found",
                    report.recordsChecked(), report.breaks().size());
        } else {
            log.info("Ledger integrity check passed: {} record(s), status {}",
                    report.recordsChecked(), report.status());
        }
        return report;
    }
}
