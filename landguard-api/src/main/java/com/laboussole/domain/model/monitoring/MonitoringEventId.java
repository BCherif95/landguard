package com.laboussole.domain.model.monitoring;

import java.util.Objects;
import java.util.UUID;

public record MonitoringEventId(UUID value) {
    public MonitoringEventId {
        Objects.requireNonNull(value);
    }

    public static MonitoringEventId generate() {
        return new MonitoringEventId(UUID.randomUUID());
    }

    public static MonitoringEventId fromString(String value) {
        return new MonitoringEventId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
