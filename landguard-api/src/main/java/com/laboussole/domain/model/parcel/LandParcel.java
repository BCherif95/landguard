package com.laboussole.domain.model.parcel;

import com.laboussole.domain.model.UserId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root for a registered land parcel.
 *
 * <p>Holds identity (cadastral reference), geometry (outer-ring polygon in WGS-84),
 * ownership, status lifecycle, and risk/trust metrics. State transitions go through
 * methods on this aggregate so invariants are co-located with the data.
 */
public final class LandParcel {

    private final ParcelId id;
    private final CadastralReference reference;
    private String name;
    private String regionLabel;
    private String ownerLabel;
    private final UserId ownerUserId;
    private Hectares area;
    private MoneyXof estimatedValue;
    private ParcelGeometry geometry;
    private ParcelStatus status;
    private int riskScore;
    private int trustScore;
    private String titleNumber;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant lastVerifiedAt;
    private final List<LandDocument> documents;

    private LandParcel(
            ParcelId id,
            CadastralReference reference,
            String name,
            String regionLabel,
            String ownerLabel,
            UserId ownerUserId,
            Hectares area,
            MoneyXof estimatedValue,
            ParcelGeometry geometry,
            ParcelStatus status,
            int riskScore,
            int trustScore,
            String titleNumber,
            Instant createdAt,
            Instant updatedAt,
            Instant lastVerifiedAt,
            List<LandDocument> documents) {
        this.id = Objects.requireNonNull(id);
        this.reference = Objects.requireNonNull(reference);
        this.name = requireText(name, "name", 200);
        this.regionLabel = requireText(regionLabel, "regionLabel", 200);
        this.ownerLabel = requireText(ownerLabel, "ownerLabel", 200);
        this.ownerUserId = ownerUserId;
        this.area = Objects.requireNonNull(area);
        this.estimatedValue = Objects.requireNonNull(estimatedValue);
        this.geometry = Objects.requireNonNull(geometry);
        this.status = Objects.requireNonNull(status);
        this.riskScore = clampScore(riskScore);
        this.trustScore = clampScore(trustScore);
        this.titleNumber = titleNumber;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.lastVerifiedAt = lastVerifiedAt;
        this.documents = new ArrayList<>(documents);
    }

    public static LandParcel register(
            CadastralReference reference,
            String name,
            String regionLabel,
            String ownerLabel,
            UserId ownerUserId,
            Hectares area,
            MoneyXof estimatedValue,
            ParcelGeometry geometry,
            List<LandDocument> documents) {
        var now = Instant.now();
        return new LandParcel(
                ParcelId.generate(),
                reference,
                name,
                regionLabel,
                ownerLabel,
                ownerUserId,
                area,
                estimatedValue,
                geometry,
                ParcelStatus.SUBMITTED,
                /* riskScore */ 50,
                /* trustScore */ 20,
                null,
                now, now, null,
                documents);
    }

    public static LandParcel reconstitute(
            ParcelId id,
            CadastralReference reference,
            String name,
            String regionLabel,
            String ownerLabel,
            UserId ownerUserId,
            Hectares area,
            MoneyXof estimatedValue,
            ParcelGeometry geometry,
            ParcelStatus status,
            int riskScore,
            int trustScore,
            String titleNumber,
            Instant createdAt,
            Instant updatedAt,
            Instant lastVerifiedAt,
            List<LandDocument> documents) {
        return new LandParcel(
                id, reference, name, regionLabel, ownerLabel, ownerUserId,
                area, estimatedValue, geometry, status,
                riskScore, trustScore, titleNumber, createdAt, updatedAt, lastVerifiedAt,
                documents);
    }

    public void transitionTo(ParcelStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException(
                    "Cannot transition parcel from " + status + " to " + target);
        }
        this.status = target;
        this.updatedAt = Instant.now();
        if (target == ParcelStatus.CERTIFIED || target == ParcelStatus.TITLE_ISSUED) {
            this.lastVerifiedAt = Instant.now();
        }
    }

    public void verifyAndCertify(int newTrustScore) {
        transitionTo(ParcelStatus.CERTIFIED);
        this.trustScore = clampScore(newTrustScore);
    }

    public void flagDispute() {
        transitionTo(ParcelStatus.DISPUTED);
    }

    public void issueTitle(String titleNumber) {
        if (status != ParcelStatus.CERTIFIED) {
            throw new IllegalStateException("Parcel must be CERTIFIED before issuing a title");
        }
        this.titleNumber = Objects.requireNonNull(titleNumber);
        transitionTo(ParcelStatus.TITLE_ISSUED);
    }

    public String titleNumber() { return titleNumber; }

    public void updateRiskScore(int newScore) {
        this.riskScore = clampScore(newScore);
        this.updatedAt = Instant.now();
    }

    public void updateTrustScore(int newScore) {
        this.trustScore = clampScore(newScore);
        this.updatedAt = Instant.now();
    }

    public void transferOwnership(String newOwnerLabel) {
        this.ownerLabel = requireText(newOwnerLabel, "ownerLabel", 200);
        this.updatedAt = Instant.now();
    }

    public RiskLevel riskLevel() {
        return RiskLevel.fromScore(riskScore);
    }

    public ParcelId id() { return id; }
    public CadastralReference reference() { return reference; }
    public String name() { return name; }
    public String regionLabel() { return regionLabel; }
    public String ownerLabel() { return ownerLabel; }
    public UserId ownerUserId() { return ownerUserId; }
    public Hectares area() { return area; }
    public MoneyXof estimatedValue() { return estimatedValue; }
    public ParcelGeometry geometry() { return geometry; }
    public ParcelStatus status() { return status; }
    public int riskScore() { return riskScore; }
    public int trustScore() { return trustScore; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Instant lastVerifiedAt() { return lastVerifiedAt; }
    public List<LandDocument> documents() { return List.copyOf(documents); }

    private static String requireText(String v, String field, int max) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        if (v.length() > max) {
            throw new IllegalArgumentException(field + " must not exceed " + max + " characters");
        }
        return v.trim();
    }

    private static int clampScore(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Score must be in [0, 100]");
        }
        return score;
    }
}
