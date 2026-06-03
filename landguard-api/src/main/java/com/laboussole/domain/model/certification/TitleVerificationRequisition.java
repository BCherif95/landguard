package com.laboussole.domain.model.certification;

import java.time.Instant;

/**
 * Result of the manual verification at "Direction Nationale des Domaines et du Cadastre".
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
        String rejectionReason
) {}
