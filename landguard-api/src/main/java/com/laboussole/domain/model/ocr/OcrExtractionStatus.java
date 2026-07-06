package com.laboussole.domain.model.ocr;

/**
 * Outcome of an OCR pass over an uploaded document.
 *
 * <p>BUSINESS CONSTRAINT (PRD, Feature 02.2 — Console d'Instruction):
 * OCR is advisory only. There is deliberately NO status meaning "validated":
 * whatever the OCR reads or flags, final validation of a document is always
 * performed by a human instructor. Do not add an auto-validated state here.
 */
public enum OcrExtractionStatus {
    /** Extraction produced; the document still awaits human verification. */
    PENDING_VERIFICATION,
    /** OCR engine failed on this document; a human must review it unaided. */
    FAILED
}
