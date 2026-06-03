package com.laboussole.interfaces.rest.parcel.dto;

import java.time.Instant;
import java.util.UUID;

public record UploadDocumentResponse(
        UUID id,
        String storageKey,
        String fileName,
        Instant uploadedAt) {
}
