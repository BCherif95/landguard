package com.laboussole.domain.model.parcel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Monetary amount in XOF (West African CFA franc). No fractional units. */
public record MoneyXof(BigDecimal amount) {

    public MoneyXof {
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        amount = amount.setScale(0, RoundingMode.HALF_UP);
    }

    public static MoneyXof of(long amount) {
        return new MoneyXof(BigDecimal.valueOf(amount));
    }

    public static MoneyXof zero() {
        return new MoneyXof(BigDecimal.ZERO);
    }

    public long asLong() {
        return amount.longValueExact();
    }
}
