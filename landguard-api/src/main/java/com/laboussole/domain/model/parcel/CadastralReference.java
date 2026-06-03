package com.laboussole.domain.model.parcel;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * National cadastral reference for a parcel.
 * Format: {COUNTRY}-{REGION}-{YEAR}-{SEQ}, e.g. {@code BSL-ML-2024-00187}.
 */
public record CadastralReference(String value) {

    private static final Pattern PATTERN =
            Pattern.compile("^[A-Z]{3}-[A-Z]{2}-\\d{4}-\\d{4,6}$");

    public CadastralReference {
        Objects.requireNonNull(value, "CadastralReference value must not be null");
        var normalised = value.trim().toUpperCase();
        if (!PATTERN.matcher(normalised).matches()) {
            throw new IllegalArgumentException(
                    "Invalid cadastral reference: " + value
                            + " (expected COUNTRY-REGION-YEAR-SEQ, e.g. BSL-ML-2024-00187)");
        }
        value = normalised;
    }

    public static CadastralReference of(String raw) {
        return new CadastralReference(raw);
    }
}
