package com.laboussole.interfaces.rest.parcel;

import com.laboussole.domain.model.ocr.OcrExtractionId;
import com.laboussole.domain.model.parcel.DocumentType;
import com.laboussole.domain.port.in.ocr.ExtractDocumentDataUseCase;
import com.laboussole.domain.port.in.ocr.GetDocumentOcrExtractionUseCase;
import com.laboussole.domain.port.out.StorageService;
import com.laboussole.interfaces.rest.parcel.dto.OcrExtractionResponse;
import com.laboussole.interfaces.rest.parcel.dto.UploadDocumentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "Land document management")
@RequiredArgsConstructor
public class DocumentController {

    private final StorageService storageService;
    private final ExtractDocumentDataUseCase extractUseCase;
    private final GetDocumentOcrExtractionUseCase getExtractionUseCase;

    @PostMapping("/upload")
    @Operation(summary = "Upload a land document. Triggers OCR extraction; the returned id "
            + "addresses the extraction via GET /documents/{id}/ocr-extraction.")
    public ResponseEntity<UploadDocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", required = false) String type) throws IOException {

        byte[] documentBytes = file.getBytes();
        String storageKey = storageService.store(
                new ByteArrayInputStream(documentBytes),
                file.getOriginalFilename(),
                file.getContentType());

        // OCR runs on every upload but is advisory only: it pre-fills the
        // wizard and flags anomalies. It never validates the document —
        // final validation is human (PRD Feature 02.2, Console d'Instruction).
        var extraction = extractUseCase.execute(new ExtractDocumentDataUseCase.Command(
                documentBytes,
                storageKey,
                DocumentType.fromLabel(type)));

        var response = new UploadDocumentResponse(
                extraction.id().value(),
                storageKey,
                file.getOriginalFilename(),
                Instant.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/ocr-extraction")
    @Operation(summary = "OCR extraction for an uploaded document (id returned by the upload).")
    public OcrExtractionResponse getOcrExtraction(@PathVariable("id") String id) {
        return OcrExtractionResponse.from(getExtractionUseCase.execute(OcrExtractionId.of(id)));
    }
}
