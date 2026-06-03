package com.laboussole.domain.model.heritage;

import java.util.Objects;
import java.util.UUID;

public record SuccessionPlanId(UUID value) {
    public SuccessionPlanId {
        Objects.requireNonNull(value);
    }

    public static SuccessionPlanId generate() {
        return new SuccessionPlanId(UUID.randomUUID());
    }

    public static SuccessionPlanId of(UUID value) {
        return new SuccessionPlanId(value);
    }

    public static SuccessionPlanId fromString(String value) {
        return new SuccessionPlanId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
