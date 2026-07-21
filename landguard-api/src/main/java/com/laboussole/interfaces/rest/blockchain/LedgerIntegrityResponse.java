package com.laboussole.interfaces.rest.blockchain;

import com.laboussole.domain.model.blockchain.LedgerBreak;
import com.laboussole.domain.model.blockchain.LedgerIntegrityReport;
import com.laboussole.domain.model.blockchain.LedgerIntegrityStatus;

import java.util.List;
import java.util.UUID;

/**
 * Verification verdict returned to the client. {@code summary} is French and
 * meant to be displayed as-is (dashboard banner, PDF evidence report).
 */
public record LedgerIntegrityResponse(
        LedgerIntegrityStatus status,
        String summary,
        long recordsChecked,
        long sealedRecords,
        long unsealedRecords,
        List<LedgerBreakResponse> breaks
) {

    public record LedgerBreakResponse(
            long chainIndex,
            UUID recordId,
            String type,
            String detail
    ) {
        static LedgerBreakResponse from(LedgerBreak brk) {
            return new LedgerBreakResponse(
                    brk.chainIndex(), brk.recordId(), brk.type().name(), brk.detail());
        }
    }

    public static LedgerIntegrityResponse from(LedgerIntegrityReport report) {
        return new LedgerIntegrityResponse(
                report.status(),
                summarise(report),
                report.recordsChecked(),
                report.sealedRecords(),
                report.unsealedRecords(),
                report.breaks().stream().map(LedgerBreakResponse::from).toList());
    }

    private static String summarise(LedgerIntegrityReport report) {
        return switch (report.status()) {
            case EMPTY ->
                    "Registre vide : aucun ancrage n'a encore été effectué.";
            case INTACT ->
                    "Registre intègre : %d entrée(s) vérifiées, tous les sceaux concordent."
                            .formatted(report.recordsChecked());
            case PARTIALLY_VERIFIABLE ->
                    ("Registre cohérent : %d entrée(s) vérifiées. %d entrée(s) antérieures au"
                            + " scellement vérifiable ne peuvent pas être recalculées ; leur"
                            + " chaînage est contrôlé, leur sceau ne l'est pas.")
                            .formatted(report.recordsChecked(), report.unsealedRecords());
            case BROKEN ->
                    ("ALERTE — Registre compromis : %d rupture(s) détectée(s) sur %d entrée(s)."
                            + " L'intégrité du registre ne peut plus être garantie.")
                            .formatted(report.breaks().size(), report.recordsChecked());
        };
    }
}
