package com.laboussole.domain.model.monitoring;

import java.util.Objects;
import java.util.UUID;

public record SatelliteSnapshotId(UUID value) {
    public SatelliteSnapshotId {
        Objects.requireNonNull(value);
    }

    public static SatelliteSnapshotId generate() {
        return new SatelliteSnapshotId(UUID.randomUUID());
    }

    public static SatelliteSnapshotId fromString(String value) {
        return new SatelliteSnapshotId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
