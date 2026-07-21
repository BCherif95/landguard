package com.laboussole.domain.model.notification;

import java.util.Objects;
import java.util.UUID;

public record AlertDeliveryId(UUID value) {

    public AlertDeliveryId {
        Objects.requireNonNull(value);
    }

    public static AlertDeliveryId generate() {
        return new AlertDeliveryId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
