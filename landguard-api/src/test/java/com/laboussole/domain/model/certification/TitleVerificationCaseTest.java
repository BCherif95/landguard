package com.laboussole.domain.model.certification;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.parcel.Hectares;
import com.laboussole.domain.model.parcel.ParcelId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * PRD Feature 02.2 — mandatory instruction checklist: a title is only verified
 * when the souche is authentic, no litigation is pending, no blocking mortgage
 * exists and the owner's name matches the national registry.
 */
class TitleVerificationCaseTest {

    private final UserId officer = UserId.generate();

    @Test
    void fullChecklistPassVerifiesTheTitle() {
        var verificationCase = caseUnderDomainControl();

        verificationCase.recordRequisition(requisition(true, false, false, true));

        assertEquals(VerificationStatus.TF_VERIFIED, verificationCase.status());
    }

    @Test
    void litigationSignalsADispute() {
        var verificationCase = caseUnderDomainControl();

        verificationCase.recordRequisition(requisition(true, true, false, true));

        assertEquals(VerificationStatus.DISPUTE_SIGNALED, verificationCase.status());
    }

    @Test
    void blockingMortgageRejectsTheTitle() {
        var verificationCase = caseUnderDomainControl();

        verificationCase.recordRequisition(requisition(true, false, true, true));

        assertEquals(VerificationStatus.TF_REJECTED, verificationCase.status());
    }

    @Test
    void nameMismatchWithNationalRegistryRejectsTheTitle() {
        var verificationCase = caseUnderDomainControl();

        verificationCase.recordRequisition(requisition(true, false, false, false));

        assertEquals(VerificationStatus.TF_REJECTED, verificationCase.status());
    }

    @Test
    void unconfirmedAuthenticityRejectsTheTitle() {
        var verificationCase = caseUnderDomainControl();

        verificationCase.recordRequisition(requisition(false, false, false, true));

        assertEquals(VerificationStatus.TF_REJECTED, verificationCase.status());
    }

    @Test
    void certificationRequiresAVerifiedTitle() {
        var verificationCase = caseUnderDomainControl();
        verificationCase.recordRequisition(requisition(true, false, true, true));

        assertThrows(IllegalStateException.class, () -> verificationCase.certify(officer));
    }

    @Test
    void certificationSucceedsAfterFullChecklistPass() {
        var verificationCase = caseUnderDomainControl();
        verificationCase.recordRequisition(requisition(true, false, false, true));

        verificationCase.certify(officer);

        assertEquals(VerificationStatus.CERTIFIED, verificationCase.status());
        assertEquals(officer, verificationCase.certifiedBy());
    }

    private TitleVerificationCase caseUnderDomainControl() {
        var verificationCase = TitleVerificationCase.open(
                ParcelId.generate(), "CERT-2026-0001", title(), UserId.generate());
        verificationCase.submitForVerification();
        verificationCase.startDomainControl();
        return verificationCase;
    }

    private LandTitle title() {
        return new LandTitle(
                "TF-4521/BKO", "Vol. 12", "Folio 87", "Bureau des Domaines de Bamako",
                Instant.parse("2015-03-12T00:00:00Z"), "Moussa Traoré",
                Hectares.of(1.5), "Bamako, Commune IV", "ACTIVE");
    }

    private TitleVerificationRequisition requisition(
            boolean authenticityConfirmed,
            boolean litigationDetected,
            boolean mortgageDetected,
            boolean nameMatchConfirmed) {
        return new TitleVerificationRequisition(
                "REQ-2026-0042",
                Instant.now(),
                "Bureau des Domaines de Bamako",
                "Aïssata Koné",
                "Agent des Domaines",
                "Contrôle de la souche effectué.",
                authenticityConfirmed,
                false,
                litigationDetected,
                mortgageDetected,
                nameMatchConfirmed,
                null);
    }
}
