package com.laboussole.domain.model.parcel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Surface area in hectares, rounded to 4 decimals (≈ m² resolution). */
public record Hectares(BigDecimal value) {

    public Hectares {
        Objects.requireNonNull(value, "Hectares value must not be null");
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Hectares must be non-negative");
        }
        value = value.setScale(4, RoundingMode.HALF_UP);
    }

    public static Hectares of(double v) {
        return new Hectares(BigDecimal.valueOf(v));
    }

    public static Hectares of(BigDecimal v) {
        return new Hectares(v);
    }

    public double asDouble() {
        return value.doubleValue();
    }
}
