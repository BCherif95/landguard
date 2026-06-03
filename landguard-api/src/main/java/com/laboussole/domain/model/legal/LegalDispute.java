package com.laboussole.domain.model.legal;

import com.laboussole.domain.model.parcel.ParcelId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class LegalDispute {
    private final UUID id;
    private final ParcelId parcelId;
    private final String reason;
    private final DisputeSeverity severity;
    private final Instant openedAt;
    private Instant resolvedAt;
    private DisputeStatus status;

    public LegalDispute(UUID id, ParcelId parcelId, String reason, DisputeSeverity severity, Instant openedAt, Instant resolvedAt, DisputeStatus status) {
        this.id = Objects.requireNonNull(id);
        this.parcelId = Objects.requireNonNull(parcelId);
        this.reason = Objects.requireNonNull(reason);
        this.severity = Objects.requireNonNull(severity);
        this.openedAt = Objects.requireNonNull(openedAt);
        this.resolvedAt = resolvedAt;
        this.status = Objects.requireNonNull(status);
    }

    public static LegalDispute open(ParcelId parcelId, String reason, DisputeSeverity severity) {
        return new LegalDispute(UUID.randomUUID(), parcelId, reason, severity, Instant.now(), null, DisputeStatus.OPEN);
    }

    public void resolve() {
        this.status = DisputeStatus.RESOLVED;
        this.resolvedAt = Instant.now();
    }

    public UUID id() { return id; }
    public ParcelId parcelId() { return parcelId; }
    public String reason() { return reason; }
    public DisputeSeverity severity() { return severity; }
    public Instant openedAt() { return openedAt; }
    public Instant resolvedAt() { return resolvedAt; }
    public DisputeStatus status() { return status; }
}
