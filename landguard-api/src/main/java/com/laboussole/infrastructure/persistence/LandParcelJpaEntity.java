package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.parcel.ParcelStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Polygon;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "land_parcels")
class LandParcelJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @Column(name = "reference", nullable = false, length = 64, unique = true)
    private String reference;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "region_label", nullable = false, length = 200)
    private String regionLabel;

    @Column(name = "owner_label", nullable = false, length = 200)
    private String ownerLabel;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "owner_user_id", length = 36)
    private UUID ownerUserId;

    @Column(name = "area_ha", nullable = false, precision = 14, scale = 4)
    private BigDecimal areaHa;

    @Column(name = "estimated_value_xof", nullable = false, precision = 18, scale = 0)
    private BigDecimal estimatedValueXof;

    @Column(name = "geometry", columnDefinition = "GEOMETRY", nullable = false)
    private Polygon geometry;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ParcelStatus status;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "trust_score", nullable = false)
    private int trustScore;

    @Column(name = "title_number", length = 32, unique = true)
    private String titleNumber;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_verified_at")
    private Instant lastVerifiedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parcel_id")
    private List<LandDocumentJpaEntity> documents = new ArrayList<>();

    protected LandParcelJpaEntity() {}

    LandParcelJpaEntity(
            UUID id, String reference, String name, String regionLabel, String ownerLabel,
            UUID ownerUserId, BigDecimal areaHa, BigDecimal estimatedValueXof,
            Polygon geometry, ParcelStatus status, int riskScore, int trustScore,
            String titleNumber,
            Instant createdAt, Instant updatedAt, Instant lastVerifiedAt,
            List<LandDocumentJpaEntity> documents) {
        this.id = id;
        this.reference = reference;
        this.name = name;
        this.regionLabel = regionLabel;
        this.ownerLabel = ownerLabel;
        this.ownerUserId = ownerUserId;
        this.areaHa = areaHa;
        this.estimatedValueXof = estimatedValueXof;
        this.geometry = geometry;
        this.status = status;
        this.riskScore = riskScore;
        this.trustScore = trustScore;
        this.titleNumber = titleNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastVerifiedAt = lastVerifiedAt;
        this.documents = documents;
    }

    UUID getId() { return id; }
    String getReference() { return reference; }
    String getName() { return name; }
    String getRegionLabel() { return regionLabel; }
    String getOwnerLabel() { return ownerLabel; }
    UUID getOwnerUserId() { return ownerUserId; }
    BigDecimal getAreaHa() { return areaHa; }
    BigDecimal getEstimatedValueXof() { return estimatedValueXof; }
    Polygon getGeometry() { return geometry; }
    ParcelStatus getStatus() { return status; }
    int getRiskScore() { return riskScore; }
    int getTrustScore() { return trustScore; }
    String getTitleNumber() { return titleNumber; }
    Instant getCreatedAt() { return createdAt; }
    Instant getUpdatedAt() { return updatedAt; }
    Instant getLastVerifiedAt() { return lastVerifiedAt; }
    List<LandDocumentJpaEntity> getDocuments() { return documents; }

    void setName(String name) { this.name = name; }
    void setRegionLabel(String regionLabel) { this.regionLabel = regionLabel; }
    void setOwnerLabel(String ownerLabel) { this.ownerLabel = ownerLabel; }
    void setAreaHa(BigDecimal areaHa) { this.areaHa = areaHa; }
    void setEstimatedValueXof(BigDecimal estimatedValueXof) { this.estimatedValueXof = estimatedValueXof; }
    void setGeometry(Polygon geometry) { this.geometry = geometry; }
    void setStatus(ParcelStatus status) { this.status = status; }
    void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    void setTrustScore(int trustScore) { this.trustScore = trustScore; }
    void setTitleNumber(String titleNumber) { this.titleNumber = titleNumber; }
    void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    void setLastVerifiedAt(Instant lastVerifiedAt) { this.lastVerifiedAt = lastVerifiedAt; }
}
