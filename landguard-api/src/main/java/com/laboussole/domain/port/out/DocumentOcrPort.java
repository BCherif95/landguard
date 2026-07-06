package com.laboussole.domain.port.out;

import com.laboussole.domain.model.ocr.OcrExtractionResult;
import com.laboussole.domain.model.parcel.DocumentType;

/**
 * Driven port: optical character recognition over an uploaded land document.
 *
 * <p>Implementations return best-effort extractions — absent fields are
 * normal, not errors. They throw on technical failure (engine unavailable,
 * unreadable bytes); the caller decides how to degrade.
 */
public interface DocumentOcrPort {

    OcrExtractionResult extract(byte[] documentBytes, DocumentType type);
}
