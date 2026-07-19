package com.laboussole.application.usecase.ocr;

import com.laboussole.domain.model.ocr.DocumentOcrExtraction;
import com.laboussole.domain.port.in.ocr.ExtractDocumentDataUseCase;
import com.laboussole.domain.port.out.DocumentOcrExtractionRepository;
import com.laboussole.domain.port.out.DocumentOcrPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExtractDocumentDataService implements ExtractDocumentDataUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExtractDocumentDataService.class);

    private final DocumentOcrPort ocrPort;
    private final DocumentOcrExtractionRepository repository;

    public ExtractDocumentDataService(DocumentOcrPort ocrPort, DocumentOcrExtractionRepository repository) {
        this.ocrPort = ocrPort;
        this.repository = repository;
    }

    // BUSINESS CONSTRAINT (PRD, Feature 02.2 — Console d'Instruction):
    // OCR output is advisory ONLY. It pre-fills form fields and flags
    // structural anomalies for the human instructor; it must NEVER validate
    // a document, change a document status, or advance a parcel lifecycle.
    // Any future change wiring this result into an automatic approval is a
    // PRD violation.
    @Override
    @Transactional
    public DocumentOcrExtraction execute(Command command) {
        DocumentOcrExtraction extraction;
        try {
            var result = ocrPort.extract(command.documentBytes(), command.documentType());
            extraction = DocumentOcrExtraction.completed(
                    command.storageKey(), command.documentType(), result);
        } catch (RuntimeException | LinkageError e) {
            // A broken OCR engine must not block the upload: record the
            // failure so the instructor reviews the document unaided.
            // LinkageError covers a missing native Tesseract library
            // (UnsatisfiedLinkError, then NoClassDefFoundError on every
            // later call once TessAPI failed to initialise).
            log.error("OCR extraction failed for storageKey={} (type={}): {}",
                    command.storageKey(), command.documentType(), e.getMessage(), e);
            extraction = DocumentOcrExtraction.failed(command.storageKey(), command.documentType());
        }
        return repository.save(extraction);
    }
}
