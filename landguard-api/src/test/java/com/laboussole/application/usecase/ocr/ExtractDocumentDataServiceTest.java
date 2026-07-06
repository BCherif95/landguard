package com.laboussole.application.usecase.ocr;

import com.laboussole.domain.model.ocr.DocumentOcrExtraction;
import com.laboussole.domain.model.ocr.OcrExtractionResult;
import com.laboussole.domain.model.ocr.OcrExtractionStatus;
import com.laboussole.domain.model.parcel.DocumentType;
import com.laboussole.domain.port.in.ocr.ExtractDocumentDataUseCase;
import com.laboussole.domain.port.out.DocumentOcrExtractionRepository;
import com.laboussole.domain.port.out.DocumentOcrPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtractDocumentDataServiceTest {

    @Mock private DocumentOcrPort ocrPort;
    @Mock private DocumentOcrExtractionRepository repository;

    private ExtractDocumentDataService service() {
        // Repository echoes back what it is asked to save.
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return new ExtractDocumentDataService(ocrPort, repository);
    }

    @Test
    void storesSuccessfulExtractionAsPendingVerification_neverValidated() {
        var result = new OcrExtractionResult(
                Optional.of("TF-2024-0042"),
                Optional.of("MOUSSA TRAORE"),
                Optional.of(new BigDecimal("2.5")),
                List.of("Police d'écriture non uniforme détectée dans le document."));
        when(ocrPort.extract(any(), any())).thenReturn(result);

        var extraction = service().execute(command());

        // The only possible post-OCR status is PENDING_VERIFICATION: the OCR
        // pre-fills and flags, a human instructor validates (PRD 02.2).
        assertThat(extraction.status()).isEqualTo(OcrExtractionStatus.PENDING_VERIFICATION);
        assertThat(extraction.result().titleNumber()).contains("TF-2024-0042");
        assertThat(extraction.hasAnomalies()).isTrue();
    }

    @Test
    void recordsFailedExtractionInsteadOfPropagatingOcrErrors() {
        when(ocrPort.extract(any(), any())).thenThrow(new RuntimeException("libtesseract missing"));

        DocumentOcrExtraction extraction = service().execute(command());

        assertThat(extraction.status()).isEqualTo(OcrExtractionStatus.FAILED);
        assertThat(extraction.result().titleNumber()).isEmpty();
        assertThat(extraction.result().structuralAnomalies()).isEmpty();
    }

    @Test
    void keepsStorageKeyAndDocumentTypeOnTheStoredExtraction() {
        when(ocrPort.extract(any(), any())).thenReturn(OcrExtractionResult.empty());

        var extraction = service().execute(command());

        assertThat(extraction.storageKey()).isEqualTo("docs/tf-scan.pdf");
        assertThat(extraction.documentType()).isEqualTo(DocumentType.TF);
    }

    private static ExtractDocumentDataUseCase.Command command() {
        return new ExtractDocumentDataUseCase.Command(
                new byte[] {1, 2, 3}, "docs/tf-scan.pdf", DocumentType.TF);
    }
}
