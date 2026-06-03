package com.laboussole.domain.model.certification;

import java.util.UUID;

public record CertificationId(UUID value) {
    public static CertificationId generate() {
        return new CertificationId(UUID.randomUUID());
    }

    public static CertificationId fromString(String value) {
        return new CertificationId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
