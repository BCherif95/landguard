package com.laboussole.domain.model.heritage;

import java.util.Objects;
import java.util.UUID;

public record HeirId(UUID value) {
    public HeirId {
        Objects.requireNonNull(value);
    }

    public static HeirId generate() {
        return new HeirId(UUID.randomUUID());
    }

    public static HeirId of(UUID value) {
        return new HeirId(value);
    }

    public static HeirId fromString(String value) {
        return new HeirId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
