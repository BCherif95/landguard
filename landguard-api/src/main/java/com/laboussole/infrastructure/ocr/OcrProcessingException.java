package com.laboussole.infrastructure.ocr;

/**
 * Technical OCR failure (engine unavailable, unreadable bytes…).
 * Caught by {@code ExtractDocumentDataService}, which records a FAILED
 * extraction instead of blocking the document upload.
 */
public class OcrProcessingException extends RuntimeException {

    public OcrProcessingException(String message) {
        super(message);
    }

    public OcrProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
