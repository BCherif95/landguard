package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.certification.TitleVerificationRequisition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "title_verification_requisitions")
@Getter
@Setter
public class TitleVerificationRequisitionJpaEntity {

    @Id
    @Column(name = "case_id")
    private UUID caseId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "case_id")
    private TitleVerificationCaseJpaEntity caseEntity;

    private String requisitionNumber;
    private Instant requisitionDate;
    private String domainOffice;
    private String verifierName;
    private String verifierRole;
    private String verificationNotes;
    private boolean authenticityConfirmed;
    private boolean conflictDetected;
    private boolean litigationDetected;
    private boolean mortgageDetected;
    private boolean nameMatchConfirmed;
    private String rejectionReason;

    public static TitleVerificationRequisitionJpaEntity fromDomain(
            TitleVerificationRequisition domain, 
            TitleVerificationCaseJpaEntity caseEntity) {
        var entity = new TitleVerificationRequisitionJpaEntity();
        entity.setCaseId(caseEntity.getId());
        entity.setCaseEntity(caseEntity);
        entity.setRequisitionNumber(domain.requisitionNumber());
        entity.setRequisitionDate(domain.requisitionDate());
        entity.setDomainOffice(domain.domainOffice());
        entity.setVerifierName(domain.verifierName());
        entity.setVerifierRole(domain.verifierRole());
        entity.setVerificationNotes(domain.verificationNotes());
        entity.setAuthenticityConfirmed(domain.authenticityConfirmed());
        entity.setConflictDetected(domain.conflictDetected());
        entity.setLitigationDetected(domain.litigationDetected());
        entity.setMortgageDetected(domain.mortgageDetected());
        entity.setNameMatchConfirmed(domain.nameMatchConfirmed());
        entity.setRejectionReason(domain.rejectionReason());
        return entity;
    }

    public TitleVerificationRequisition toDomain() {
        return new TitleVerificationRequisition(
                requisitionNumber,
                requisitionDate,
                domainOffice,
                verifierName,
                verifierRole,
                verificationNotes,
                authenticityConfirmed,
                conflictDetected,
                litigationDetected,
                mortgageDetected,
                nameMatchConfirmed,
                rejectionReason
        );
    }
}
