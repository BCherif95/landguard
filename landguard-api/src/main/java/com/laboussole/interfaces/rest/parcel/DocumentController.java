package com.laboussole.interfaces.rest.parcel;

import com.laboussole.domain.port.out.StorageService;
import com.laboussole.interfaces.rest.parcel.dto.UploadDocumentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "Land document management")
@RequiredArgsConstructor
public class DocumentController {

    private final StorageService storageService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a land document")
    public ResponseEntity<UploadDocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file) throws IOException {
        
        String storageKey = storageService.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType());
        
        var response = new UploadDocumentResponse(
                UUID.randomUUID(),
                storageKey,
                file.getOriginalFilename(),
                Instant.now());
        
        return ResponseEntity.ok(response);
    }
}
