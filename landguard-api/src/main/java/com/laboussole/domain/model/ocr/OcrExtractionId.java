package com.laboussole.domain.model.ocr;

import java.util.Objects;
import java.util.UUID;

public record OcrExtractionId(UUID value) {

    public OcrExtractionId {
        Objects.requireNonNull(value, "OcrExtractionId value must not be null");
    }

    public static OcrExtractionId generate() {
        return new OcrExtractionId(UUID.randomUUID());
    }

    public static OcrExtractionId of(UUID value) {
        return new OcrExtractionId(value);
    }

    public static OcrExtractionId of(String value) {
        return new OcrExtractionId(UUID.fromString(value));
    }
}
