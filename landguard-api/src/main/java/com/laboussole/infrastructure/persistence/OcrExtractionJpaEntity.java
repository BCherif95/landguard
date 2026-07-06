package com.laboussole.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ocr_extraction_results")
class OcrExtractionJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @Column(name = "storage_key", nullable = false, length = 255, unique = true)
    private String storageKey;

    @Column(name = "document_type", nullable = false, length = 20)
    private String documentType;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "title_number", length = 64)
    private String titleNumber;

    @Column(name = "owner_name", length = 200)
    private String ownerName;

    @Column(name = "surface_area_hectares", precision = 12, scale = 4)
    private BigDecimal surfaceAreaHectares;

    /** French anomaly sentences, newline-separated (none contain newlines). */
    @Column(name = "structural_anomalies", columnDefinition = "TEXT")
    private String structuralAnomalies;

    @Column(name = "extracted_at", nullable = false)
    private Instant extractedAt;

    protected OcrExtractionJpaEntity() {}

    OcrExtractionJpaEntity(
            UUID id,
            String storageKey,
            String documentType,
            String status,
            String titleNumber,
            String ownerName,
            BigDecimal surfaceAreaHectares,
            String structuralAnomalies,
            Instant extractedAt) {
        this.id = id;
        this.storageKey = storageKey;
        this.documentType = documentType;
        this.status = status;
        this.titleNumber = titleNumber;
        this.ownerName = ownerName;
        this.surfaceAreaHectares = surfaceAreaHectares;
        this.structuralAnomalies = structuralAnomalies;
        this.extractedAt = extractedAt;
    }

    UUID getId() { return id; }
    String getStorageKey() { return storageKey; }
    String getDocumentType() { return documentType; }
    String getStatus() { return status; }
    String getTitleNumber() { return titleNumber; }
    String getOwnerName() { return ownerName; }
    BigDecimal getSurfaceAreaHectares() { return surfaceAreaHectares; }
    String getStructuralAnomalies() { return structuralAnomalies; }
    Instant getExtractedAt() { return extractedAt; }
}
