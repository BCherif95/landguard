package com.laboussole.interfaces.rest.parcel.dto;

import com.laboussole.domain.model.ocr.DocumentOcrExtraction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * OCR reading of an uploaded document. Advisory only: pre-fills the wizard
 * and flags anomalies — never a validation verdict (PRD Feature 02.2).
 */
public record OcrExtractionResponse(
        UUID id,
        String storageKey,
        String documentType,
        String status,
        String titleNumber,
        String ownerName,
        BigDecimal surfaceAreaHectares,
        List<String> structuralAnomalies,
        Instant extractedAt) {

    public static OcrExtractionResponse from(DocumentOcrExtraction extraction) {
        var result = extraction.result();
        return new OcrExtractionResponse(
                extraction.id().value(),
                extraction.storageKey(),
                extraction.documentType().name(),
                extraction.status().name(),
                result.titleNumber().orElse(null),
                result.ownerName().orElse(null),
                result.surfaceAreaHectares().orElse(null),
                result.structuralAnomalies(),
                extraction.extractedAt());
    }
}
