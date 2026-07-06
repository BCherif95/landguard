package com.laboussole.domain.port.out;

import com.laboussole.domain.model.ocr.DocumentOcrExtraction;
import com.laboussole.domain.model.ocr.OcrExtractionId;

import java.util.Optional;

/** Driven port: persistence for {@link DocumentOcrExtraction}. */
public interface DocumentOcrExtractionRepository {

    DocumentOcrExtraction save(DocumentOcrExtraction extraction);

    Optional<DocumentOcrExtraction> findById(OcrExtractionId id);

    /** Lookup by the durable link shared with {@code land_documents.storage_key}. */
    Optional<DocumentOcrExtraction> findByStorageKey(String storageKey);
}
