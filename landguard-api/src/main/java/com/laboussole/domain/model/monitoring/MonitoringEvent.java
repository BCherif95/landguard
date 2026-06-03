package com.laboussole.domain.model.monitoring;

import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.model.parcel.ParcelGeometry;

import java.time.Instant;
import java.util.Objects;

public final class MonitoringEvent {
    private final MonitoringEventId id;
    private final ParcelId parcelId;
    private final MonitoringEventType type;
    private final MonitoringSeverity severity;
    private final Instant detectedAt;
    private final int confidenceScore;
    private final ParcelGeometry.Coordinate coordinates;
    private final String description;
    private final MonitoringSource source;
    private final String imageUrl;
    private boolean resolved;

    public MonitoringEvent(
            MonitoringEventId id,
            ParcelId parcelId,
            MonitoringEventType type,
            MonitoringSeverity severity,
            Instant detectedAt,
            int confidenceScore,
            ParcelGeometry.Coordinate coordinates,
            String description,
            MonitoringSource source,
            String imageUrl,
            boolean resolved) {
        this.id = Objects.requireNonNull(id);
        this.parcelId = Objects.requireNonNull(parcelId);
        this.type = Objects.requireNonNull(type);
        this.severity = Objects.requireNonNull(severity);
        this.detectedAt = Objects.requireNonNull(detectedAt);
        this.confidenceScore = clampScore(confidenceScore);
        this.coordinates = Objects.requireNonNull(coordinates);
        this.description = Objects.requireNonNull(description);
        this.source = Objects.requireNonNull(source);
        this.imageUrl = imageUrl;
        this.resolved = resolved;
    }

    public static MonitoringEvent create(
            ParcelId parcelId,
            MonitoringEventType type,
            MonitoringSeverity severity,
            int confidenceScore,
            ParcelGeometry.Coordinate coordinates,
            String description,
            MonitoringSource source,
            String imageUrl) {
        return new MonitoringEvent(
                MonitoringEventId.generate(),
                parcelId,
                type,
                severity,
                Instant.now(),
                confidenceScore,
                coordinates,
                description,
                source,
                imageUrl,
                false
        );
    }

    public void resolve() {
        this.resolved = true;
    }

    private static int clampScore(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Score must be in [0, 100]");
        }
        return score;
    }

    public MonitoringEventId id() { return id; }
    public ParcelId parcelId() { return parcelId; }
    public MonitoringEventType type() { return type; }
    public MonitoringSeverity severity() { return severity; }
    public Instant detectedAt() { return detectedAt; }
    public int confidenceScore() { return confidenceScore; }
    public ParcelGeometry.Coordinate coordinates() { return coordinates; }
    public String description() { return description; }
    public MonitoringSource source() { return source; }
    public String imageUrl() { return imageUrl; }
    public boolean resolved() { return resolved; }
}
