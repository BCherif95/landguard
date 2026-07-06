package com.laboussole.infrastructure.ocr;

import com.laboussole.domain.model.parcel.DocumentType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Pure text parsing of OCR output from Malian land documents (Titre Foncier,
 * Lettre d'Attribution…). No Tesseract dependency, fully unit-testable.
 *
 * <p>Matching runs on a diacritics-stripped upper-case copy of the text so
 * OCR confusion between {@code É}/{@code E} etc. does not break keywords,
 * while captured values are returned from the original text.
 */
final class OcrTextFieldExtractor {

    /** e.g. "TITRE FONCIER N° 12345/BKO" or "TF No: ML-2024-0042". */
    private static final Pattern TITLE_NUMBER = Pattern.compile(
            "(?:TITRE\\s+FONCIER(?:\\s+DEMATERIALISE)?|\\bT\\.?F\\.?\\b)"
                    + "\\s*(?:N[°O0º]?\\s*[:.\\-]?\\s*)?([A-Z0-9][A-Z0-9\\-/]{2,30})");

    /** Owner introduced by a registry keyword, value = rest of the line. */
    private static final Pattern OWNER_NAME = Pattern.compile(
            "(?:PROPRIETAIRE|ATTRIBUTAIRE|BENEFICIAIRE)\\s*[:.\\-]?\\s+([A-Z][A-Z' \\-]{2,80})");

    /** e.g. "SUPERFICIE : 2,5 HECTARES" or "SUPERFICIE 25000 M2". */
    private static final Pattern SURFACE_AREA = Pattern.compile(
            "SUPERFICIE\\s*[:.\\-]?\\s*([0-9]+(?:[.,][0-9]+)?)\\s*(HECTARES?|HA\\b|M2|M²|METRES?\\s+CARRES?)");

    private static final BigDecimal SQUARE_METRES_PER_HECTARE = BigDecimal.valueOf(10_000);

    record ExtractedFields(
            Optional<String> titleNumber,
            Optional<String> ownerName,
            Optional<BigDecimal> surfaceAreaHectares) {}

    private OcrTextFieldExtractor() {
    }

    static ExtractedFields extract(String rawText) {
        var text = normalise(rawText);

        Optional<String> titleNumber = firstGroup(TITLE_NUMBER, text)
                .map(v -> v.replaceAll("[.,;]+$", ""));

        Optional<String> ownerName = firstGroup(OWNER_NAME, text)
                .map(v -> v.replaceAll("\\s{2,}.*$", "").trim())
                .filter(v -> v.length() >= 3);

        Optional<BigDecimal> surface = Optional.empty();
        var surfaceMatcher = SURFACE_AREA.matcher(text);
        if (surfaceMatcher.find()) {
            var amount = new BigDecimal(surfaceMatcher.group(1).replace(',', '.'));
            var unit = surfaceMatcher.group(2);
            surface = Optional.of(unit.startsWith("H")
                    ? amount
                    : amount.divide(SQUARE_METRES_PER_HECTARE, 4, RoundingMode.HALF_UP));
        }

        return new ExtractedFields(titleNumber, ownerName, surface);
    }

    /**
     * Anomalies stemming from fields a document of this type must carry.
     * Messages are user-facing French (shown in the instruction console).
     */
    static List<String> missingFieldAnomalies(ExtractedFields fields, DocumentType type) {
        List<String> anomalies = new ArrayList<>();
        if (type == DocumentType.TF && fields.titleNumber().isEmpty()) {
            anomalies.add("Numéro de titre foncier illisible ou absent du document.");
        }
        return anomalies;
    }

    private static Optional<String> firstGroup(Pattern pattern, String text) {
        var matcher = pattern.matcher(text);
        return matcher.find() ? Optional.of(matcher.group(1).trim()) : Optional.empty();
    }

    private static String normalise(String rawText) {
        if (rawText == null) {
            return "";
        }
        var decomposed = Normalizer.normalize(rawText, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "").toUpperCase();
    }
}
