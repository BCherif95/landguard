package com.laboussole.domain.model.certification;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.parcel.ParcelId;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root for a Land Title Verification Case (Mali context).
 */
public final class TitleVerificationCase {
    private final CertificationId id;
    private final ParcelId parcelId;
    private final String caseReference;
    private VerificationStatus status;
    private final LandTitle reportedTitle;
    private TitleVerificationRequisition requisition;
    private final UserId initiatedBy;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant certifiedAt;
    private UserId certifiedBy;

    private TitleVerificationCase(
            CertificationId id,
            ParcelId parcelId,
            String caseReference,
            VerificationStatus status,
            LandTitle reportedTitle,
            UserId initiatedBy,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.parcelId = Objects.requireNonNull(parcelId);
        this.caseReference = Objects.requireNonNull(caseReference);
        this.status = Objects.requireNonNull(status);
        this.reportedTitle = Objects.requireNonNull(reportedTitle);
        this.initiatedBy = Objects.requireNonNull(initiatedBy);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static TitleVerificationCase open(ParcelId parcelId, String caseRef, LandTitle title, UserId user) {
        var now = Instant.now();
        return new TitleVerificationCase(
                CertificationId.generate(),
                parcelId,
                caseRef,
                VerificationStatus.DRAFT,
                title,
                user,
                now, now
        );
    }

    public void submitForVerification() {
        transitionTo(VerificationStatus.PENDING_VERIFICATION);
    }

    public void startDomainControl() {
        transitionTo(VerificationStatus.DOMAIN_CONTROL);
    }

    public void recordRequisition(TitleVerificationRequisition requisition) {
        this.requisition = Objects.requireNonNull(requisition);
        if (requisition.authenticityConfirmed() && !requisition.litigationDetected()) {
            transitionTo(VerificationStatus.TF_VERIFIED);
        } else if (requisition.litigationDetected()) {
            transitionTo(VerificationStatus.DISPUTE_SIGNALED);
        } else {
            transitionTo(VerificationStatus.TF_REJECTED);
        }
        this.updatedAt = Instant.now();
    }

    public void certify(UserId authority) {
        if (status != VerificationStatus.TF_VERIFIED) {
            throw new IllegalStateException("Cannot certify a title that is not verified");
        }
        transitionTo(VerificationStatus.CERTIFIED);
        this.certifiedBy = authority;
        this.certifiedAt = Instant.now();
    }

    private void transitionTo(VerificationStatus nextStatus) {
        if (!status.canTransitionTo(nextStatus)) {
            throw new IllegalStateException("Cannot transition from " + status + " to " + nextStatus);
        }
        this.status = nextStatus;
        this.updatedAt = Instant.now();
    }

    public CertificationId id() { return id; }
    public ParcelId parcelId() { return parcelId; }
    public String caseReference() { return caseReference; }
    public VerificationStatus status() { return status; }
    public LandTitle reportedTitle() { return reportedTitle; }
    public TitleVerificationRequisition requisition() { return requisition; }
    public UserId initiatedBy() { return initiatedBy; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Instant certifiedAt() { return certifiedAt; }
    public UserId certifiedBy() { return certifiedBy; }

    public static TitleVerificationCase reconstitute(
            CertificationId id,
            ParcelId parcelId,
            String caseReference,
            VerificationStatus status,
            LandTitle reportedTitle,
            TitleVerificationRequisition requisition,
            UserId initiatedBy,
            Instant createdAt,
            Instant updatedAt,
            Instant certifiedAt,
            UserId certifiedBy) {
        var caseObj = new TitleVerificationCase(id, parcelId, caseReference, status, reportedTitle, initiatedBy, createdAt, updatedAt);
        caseObj.requisition = requisition;
        caseObj.certifiedAt = certifiedAt;
        caseObj.certifiedBy = certifiedBy;
        return caseObj;
    }
}
