package com.laboussole.domain.model;

import java.util.Objects;

/**
 * Opaque wrapper around a password hash. The domain never sees the plaintext —
 * hashing/verification happens through the {@code PasswordHasher} port.
 */
public record HashedPassword(String value) {

    public HashedPassword {
        Objects.requireNonNull(value, "Hashed password must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Hashed password must not be blank");
        }
    }
}
