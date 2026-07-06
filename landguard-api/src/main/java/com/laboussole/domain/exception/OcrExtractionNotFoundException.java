package com.laboussole.domain.exception;

import com.laboussole.domain.model.ocr.OcrExtractionId;

public class OcrExtractionNotFoundException extends DomainException {

    public static final String CODE = "OCR_EXTRACTION_NOT_FOUND";

    public OcrExtractionNotFoundException(OcrExtractionId id) {
        super(CODE, "OCR extraction not found: " + id.value());
    }
}
