package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.certification.*;
import com.laboussole.domain.model.parcel.Hectares;
import com.laboussole.domain.model.parcel.ParcelId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "title_verification_cases")
@Getter
@Setter
public class TitleVerificationCaseJpaEntity {

    @Id
    private UUID id;

    @Column(name = "parcel_id", nullable = false)
    private UUID parcelId;

    @Column(name = "case_reference", unique = true, nullable = false)
    private String caseReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;

    // Reported Land Title
    private String tfNumber;
    private String tfVolume;
    private String tfFolio;
    private String tfConservationOffice;
    private Instant tfIssueDate;
    private String tfOwnerName;
    private java.math.BigDecimal tfAreaHectares;
    private String tfLocation;

    @Column(name = "initiated_by", nullable = false)
    private UUID initiatedBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "certified_at")
    private Instant certifiedAt;

    @Column(name = "certified_by")
    private UUID certifiedBy;

    @OneToOne(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private TitleVerificationRequisitionJpaEntity requisition;

    public static TitleVerificationCaseJpaEntity fromDomain(TitleVerificationCase domain) {
        var entity = new TitleVerificationCaseJpaEntity();
        entity.setId(domain.id().value());
        entity.setParcelId(domain.parcelId().value());
        entity.setCaseReference(domain.caseReference());
        entity.setStatus(domain.status());
        
        var title = domain.reportedTitle();
        entity.setTfNumber(title.tfNumber());
        entity.setTfVolume(title.volume());
        entity.setTfFolio(title.folio());
        entity.setTfConservationOffice(title.conservationOffice());
        entity.setTfIssueDate(title.issueDate());
        entity.setTfOwnerName(title.ownerName());
        entity.setTfAreaHectares(title.area().value());
        entity.setTfLocation(title.location());

        entity.setInitiatedBy(domain.initiatedBy().value());
        entity.setCreatedAt(domain.createdAt());
        entity.setUpdatedAt(domain.updatedAt());
        entity.setCertifiedAt(domain.certifiedAt());
        entity.setCertifiedBy(domain.certifiedBy() != null ? domain.certifiedBy().value() : null);

        if (domain.requisition() != null) {
            entity.setRequisition(TitleVerificationRequisitionJpaEntity.fromDomain(domain.requisition(), entity));
        }
        
        return entity;
    }

    public TitleVerificationCase toDomain() {
        var title = new LandTitle(
                tfNumber, tfVolume, tfFolio, tfConservationOffice,
                tfIssueDate, tfOwnerName, new Hectares(tfAreaHectares), tfLocation, "REPORTED"
        );

        return TitleVerificationCase.reconstitute(
                new CertificationId(id),
                new ParcelId(parcelId),
                caseReference,
                status,
                title,
                requisition != null ? requisition.toDomain() : null,
                new UserId(initiatedBy),
                createdAt,
                updatedAt,
                certifiedAt,
                certifiedBy != null ? new UserId(certifiedBy) : null
        );
    }
}
