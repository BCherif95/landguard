package com.laboussole.domain.model.ocr;

import com.laboussole.domain.model.parcel.DocumentType;

import java.time.Instant;
import java.util.Objects;

/**
 * Persistent record of one OCR pass over an uploaded land document, linked to
 * the {@code LandDocument} through its durable {@code storageKey} (the
 * document row itself is only created later, at parcel registration).
 *
 * <p>BUSINESS CONSTRAINT (PRD, Feature 02.2 — Console d'Instruction):
 * this aggregate NEVER validates a document. It only pre-fills form fields
 * and flags structural anomalies; the final decision belongs to a human
 * instructor. No method here may mutate a document or parcel status.
 */
public final class DocumentOcrExtraction {

    private final OcrExtractionId id;
    private final String storageKey;
    private final DocumentType documentType;
    private final OcrExtractionStatus status;
    private final OcrExtractionResult result;
    private final Instant extractedAt;

    private DocumentOcrExtraction(
            OcrExtractionId id,
            String storageKey,
            DocumentType documentType,
            OcrExtractionStatus status,
            OcrExtractionResult result,
            Instant extractedAt) {
        this.id = Objects.requireNonNull(id);
        this.storageKey = requireStorageKey(storageKey);
        this.documentType = Objects.requireNonNull(documentType);
        this.status = Objects.requireNonNull(status);
        this.result = Objects.requireNonNull(result);
        this.extractedAt = Objects.requireNonNull(extractedAt);
    }

    /** Successful OCR pass — still awaiting human verification. */
    public static DocumentOcrExtraction completed(
            String storageKey, DocumentType documentType, OcrExtractionResult result) {
        return new DocumentOcrExtraction(
                OcrExtractionId.generate(), storageKey, documentType,
                OcrExtractionStatus.PENDING_VERIFICATION, result, Instant.now());
    }

    /** OCR engine failure — nothing extracted, human review proceeds unaided. */
    public static DocumentOcrExtraction failed(String storageKey, DocumentType documentType) {
        return new DocumentOcrExtraction(
                OcrExtractionId.generate(), storageKey, documentType,
                OcrExtractionStatus.FAILED, OcrExtractionResult.empty(), Instant.now());
    }

    public static DocumentOcrExtraction reconstitute(
            OcrExtractionId id,
            String storageKey,
            DocumentType documentType,
            OcrExtractionStatus status,
            OcrExtractionResult result,
            Instant extractedAt) {
        return new DocumentOcrExtraction(id, storageKey, documentType, status, result, extractedAt);
    }

    public boolean hasAnomalies() {
        return !result.structuralAnomalies().isEmpty();
    }

    public OcrExtractionId id() { return id; }
    public String storageKey() { return storageKey; }
    public DocumentType documentType() { return documentType; }
    public OcrExtractionStatus status() { return status; }
    public OcrExtractionResult result() { return result; }
    public Instant extractedAt() { return extractedAt; }

    private static String requireStorageKey(String v) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException("storageKey must not be blank");
        }
        return v;
    }
}
