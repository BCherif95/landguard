package com.laboussole.interfaces.rest.certification.dto;

import com.laboussole.domain.model.certification.TitleVerificationCase;
import com.laboussole.domain.model.certification.TitleVerificationRequisition;

import java.time.Instant;
import java.util.UUID;

public record TitleVerificationResponse(
        UUID id,
        UUID parcelId,
        String caseReference,
        String status,
        LandTitleDto reportedTitle,
        RequisitionDto requisition,
        UUID initiatedBy,
        Instant createdAt,
        Instant updatedAt,
        Instant certifiedAt,
        UUID certifiedBy
) {
    public record RequisitionDto(
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
        public static RequisitionDto fromDomain(TitleVerificationRequisition domain) {
            if (domain == null) return null;
            return new RequisitionDto(
                    domain.requisitionNumber(),
                    domain.requisitionDate(),
                    domain.domainOffice(),
                    domain.verifierName(),
                    domain.verifierRole(),
                    domain.verificationNotes(),
                    domain.authenticityConfirmed(),
                    domain.conflictDetected(),
                    domain.litigationDetected(),
                    domain.mortgageDetected(),
                    domain.nameMatchConfirmed(),
                    domain.rejectionReason()
            );
        }

        public TitleVerificationRequisition toDomain() {
            return new TitleVerificationRequisition(
                    requisitionNumber, requisitionDate, domainOffice,
                    verifierName, verifierRole, verificationNotes,
                    authenticityConfirmed, conflictDetected, litigationDetected,
                    mortgageDetected, nameMatchConfirmed,
                    rejectionReason
            );
        }
    }

    public static TitleVerificationResponse fromDomain(TitleVerificationCase domain) {
        return new TitleVerificationResponse(
                domain.id().value(),
                domain.parcelId().value(),
                domain.caseReference(),
                domain.status().name(),
                LandTitleDto.fromDomain(domain.reportedTitle()),
                RequisitionDto.fromDomain(domain.requisition()),
                domain.initiatedBy().value(),
                domain.createdAt(),
                domain.updatedAt(),
                domain.certifiedAt(),
                domain.certifiedBy() != null ? domain.certifiedBy().value() : null
        );
    }
}
