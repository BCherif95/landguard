package com.laboussole.domain.port.in.ocr;

import com.laboussole.domain.model.ocr.DocumentOcrExtraction;
import com.laboussole.domain.model.ocr.OcrExtractionId;

public interface GetDocumentOcrExtractionUseCase {

    DocumentOcrExtraction execute(OcrExtractionId id);
}
