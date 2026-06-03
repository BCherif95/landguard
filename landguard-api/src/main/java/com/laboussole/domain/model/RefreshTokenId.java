package com.laboussole.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Strongly-typed identifier for a {@link RefreshToken}. */
public record RefreshTokenId(UUID value) {

    public RefreshTokenId {
        Objects.requireNonNull(value, "RefreshTokenId value must not be null");
    }

    public static RefreshTokenId generate() {
        return new RefreshTokenId(UUID.randomUUID());
    }

    public static RefreshTokenId of(UUID value) {
        return new RefreshTokenId(value);
    }

    public String asString() {
        return value.toString();
    }
}
