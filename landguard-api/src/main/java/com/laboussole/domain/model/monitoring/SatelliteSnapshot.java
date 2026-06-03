package com.laboussole.domain.model.monitoring;

import com.laboussole.domain.model.parcel.ParcelId;

import java.time.Instant;
import java.util.Objects;

public final class SatelliteSnapshot {
    private final SatelliteSnapshotId id;
    private final ParcelId parcelId;
    private final Instant capturedAt;
    private final String imageUrl;
    private final int movementScore;
    private final int anomalyScore;
    private final String metadata;

    public SatelliteSnapshot(
            SatelliteSnapshotId id,
            ParcelId parcelId,
            Instant capturedAt,
            String imageUrl,
            int movementScore,
            int anomalyScore,
            String metadata) {
        this.id = Objects.requireNonNull(id);
        this.parcelId = Objects.requireNonNull(parcelId);
        this.capturedAt = Objects.requireNonNull(capturedAt);
        this.imageUrl = Objects.requireNonNull(imageUrl);
        this.movementScore = clampScore(movementScore);
        this.anomalyScore = clampScore(anomalyScore);
        this.metadata = metadata;
    }

    public static SatelliteSnapshot create(
            ParcelId parcelId,
            String imageUrl,
            int movementScore,
            int anomalyScore,
            String metadata) {
        return new SatelliteSnapshot(
                SatelliteSnapshotId.generate(),
                parcelId,
                Instant.now(),
                imageUrl,
                movementScore,
                anomalyScore,
                metadata
        );
    }

    private static int clampScore(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Score must be in [0, 100]");
        }
        return score;
    }

    public SatelliteSnapshotId id() { return id; }
    public ParcelId parcelId() { return parcelId; }
    public Instant capturedAt() { return capturedAt; }
    public String imageUrl() { return imageUrl; }
    public int movementScore() { return movementScore; }
    public int anomalyScore() { return anomalyScore; }
    public String metadata() { return metadata; }
}
