package com.laboussole.domain.model.parcel;

import java.util.Objects;
import java.util.UUID;

public record ParcelId(UUID value) {

    public ParcelId {
        Objects.requireNonNull(value, "ParcelId value must not be null");
    }

    public static ParcelId generate() {
        return new ParcelId(UUID.randomUUID());
    }

    public static ParcelId of(UUID value) {
        return new ParcelId(value);
    }

    public static ParcelId of(String value) {
        return new ParcelId(UUID.fromString(value));
    }

    public String asString() {
        return value.toString();
    }
}
