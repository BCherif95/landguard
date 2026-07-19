package com.laboussole.domain.model.certification;

import java.time.Instant;

/**
 * Result of the manual verification at "Direction Nationale des Domaines et du Cadastre".
 *
 * <p>Carries the mandatory instruction checklist (PRD Feature 02.2): no pending
 * litigation, no blocking mortgage, and the buyer's name matching the national
 * registry. A title can only reach {@code TF_VERIFIED} when every check passes.
 */
public record TitleVerificationRequisition(
        String requisitionNumber,
        Instant requisitionDate,
        String domainOffice,
        String verifierName,
        String verifierRole,
        String verificationNotes,
        boolean authenticityConfirmed,
        boolean conflictDetected,
        boolean litigationDetected,
        boolean mortgageDetected,
        boolean nameMatchConfirmed,
        String rejectionReason
) {

    /** True when every mandatory checklist item allows certification. */
    public boolean checklistPassed() {
        return authenticityConfirmed && !litigationDetected && !mortgageDetected && nameMatchConfirmed;
    }
}
