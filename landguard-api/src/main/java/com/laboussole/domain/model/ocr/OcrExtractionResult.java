package com.laboussole.domain.model.ocr;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * What the OCR engine managed to read from a land document: the fields used
 * to pre-fill the registration form, plus structural anomalies worth a human
 * instructor's attention (inconsistent typeface, suspicious stamp alignment…).
 *
 * <p>Anomaly entries are user-facing French sentences — they are displayed
 * as-is in the instruction console and the registration wizard.
 */
public record OcrExtractionResult(
        Optional<String> titleNumber,
        Optional<String> ownerName,
        Optional<BigDecimal> surfaceAreaHectares,
        List<String> structuralAnomalies) {

    public OcrExtractionResult {
        Objects.requireNonNull(titleNumber);
        Objects.requireNonNull(ownerName);
        Objects.requireNonNull(surfaceAreaHectares);
        structuralAnomalies = List.copyOf(Objects.requireNonNull(structuralAnomalies));
    }

    public static OcrExtractionResult empty() {
        return new OcrExtractionResult(Optional.empty(), Optional.empty(), Optional.empty(), List.of());
    }
}
