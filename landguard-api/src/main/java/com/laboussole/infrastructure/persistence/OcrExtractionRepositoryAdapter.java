package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.ocr.DocumentOcrExtraction;
import com.laboussole.domain.model.ocr.OcrExtractionId;
import com.laboussole.domain.model.ocr.OcrExtractionResult;
import com.laboussole.domain.model.ocr.OcrExtractionStatus;
import com.laboussole.domain.model.parcel.DocumentType;
import com.laboussole.domain.port.out.DocumentOcrExtractionRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
class OcrExtractionRepositoryAdapter implements DocumentOcrExtractionRepository {

    private static final String ANOMALY_SEPARATOR = "\n";

    private final OcrExtractionSpringDataRepository jpa;

    OcrExtractionRepositoryAdapter(OcrExtractionSpringDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public DocumentOcrExtraction save(DocumentOcrExtraction extraction) {
        var result = extraction.result();
        var entity = new OcrExtractionJpaEntity(
                extraction.id().value(),
                extraction.storageKey(),
                extraction.documentType().name(),
                extraction.status().name(),
                result.titleNumber().orElse(null),
                result.ownerName().orElse(null),
                result.surfaceAreaHectares().orElse(null),
                result.structuralAnomalies().isEmpty()
                        ? null
                        : String.join(ANOMALY_SEPARATOR, result.structuralAnomalies()),
                extraction.extractedAt());
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<DocumentOcrExtraction> findById(OcrExtractionId id) {
        return jpa.findById(id.value()).map(OcrExtractionRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<DocumentOcrExtraction> findByStorageKey(String storageKey) {
        return jpa.findByStorageKey(storageKey).map(OcrExtractionRepositoryAdapter::toDomain);
    }

    private static DocumentOcrExtraction toDomain(OcrExtractionJpaEntity e) {
        List<String> anomalies = e.getStructuralAnomalies() == null || e.getStructuralAnomalies().isBlank()
                ? List.of()
                : Arrays.asList(e.getStructuralAnomalies().split(ANOMALY_SEPARATOR));
        var result = new OcrExtractionResult(
                Optional.ofNullable(e.getTitleNumber()),
                Optional.ofNullable(e.getOwnerName()),
                Optional.ofNullable(e.getSurfaceAreaHectares()),
                anomalies);
        return DocumentOcrExtraction.reconstitute(
                OcrExtractionId.of(e.getId()),
                e.getStorageKey(),
                DocumentType.fromLabel(e.getDocumentType()),
                OcrExtractionStatus.valueOf(e.getStatus()),
                result,
                e.getExtractedAt());
    }
}
