package com.laboussole.interfaces.rest.monitoring.dto;

import com.laboussole.domain.model.monitoring.SatelliteSnapshot;

import java.time.Instant;
import java.util.UUID;

public record SatelliteSnapshotResponse(
        UUID id,
        UUID parcelId,
        Instant capturedAt,
        String imageUrl,
        int movementScore,
        int anomalyScore,
        String metadata
) {
    public static SatelliteSnapshotResponse fromDomain(SatelliteSnapshot domain) {
        return new SatelliteSnapshotResponse(
                domain.id().value(),
                domain.parcelId().value(),
                domain.capturedAt(),
                domain.imageUrl(),
                domain.movementScore(),
                domain.anomalyScore(),
                domain.metadata()
        );
    }
}
