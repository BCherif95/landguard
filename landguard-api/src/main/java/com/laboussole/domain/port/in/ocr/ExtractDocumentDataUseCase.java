package com.laboussole.domain.port.in.ocr;

import com.laboussole.domain.model.ocr.DocumentOcrExtraction;
import com.laboussole.domain.model.parcel.DocumentType;

/**
 * Runs OCR over a freshly uploaded document and records the result for the
 * instruction console. Advisory only — never validates the document.
 */
public interface ExtractDocumentDataUseCase {

    DocumentOcrExtraction execute(Command command);

    record Command(byte[] documentBytes, String storageKey, DocumentType documentType) {}
}
