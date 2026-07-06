package com.laboussole.application.usecase.ocr;

import com.laboussole.domain.exception.OcrExtractionNotFoundException;
import com.laboussole.domain.model.ocr.DocumentOcrExtraction;
import com.laboussole.domain.model.ocr.OcrExtractionId;
import com.laboussole.domain.port.in.ocr.GetDocumentOcrExtractionUseCase;
import com.laboussole.domain.port.out.DocumentOcrExtractionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetDocumentOcrExtractionService implements GetDocumentOcrExtractionUseCase {

    private final DocumentOcrExtractionRepository repository;

    public GetDocumentOcrExtractionService(DocumentOcrExtractionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentOcrExtraction execute(OcrExtractionId id) {
        return repository.findById(id)
                .orElseThrow(() -> new OcrExtractionNotFoundException(id));
    }
}
