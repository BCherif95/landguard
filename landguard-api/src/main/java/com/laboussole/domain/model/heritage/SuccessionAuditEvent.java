package com.laboussole.domain.model.heritage;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record SuccessionAuditEvent(
        UUID id,
        SuccessionPlanId successionPlanId,
        SuccessionAuditType type,
        String actor,
        Instant createdAt,
        Map<String, String> metadata
) {
    public SuccessionAuditEvent {
        Objects.requireNonNull(id);
        Objects.requireNonNull(successionPlanId);
        Objects.requireNonNull(type);
        Objects.requireNonNull(actor);
        Objects.requireNonNull(createdAt);
    }

    public static SuccessionAuditEvent record(SuccessionPlanId planId, SuccessionAuditType type, String actor, Map<String, String> metadata) {
        return new SuccessionAuditEvent(UUID.randomUUID(), planId, type, actor, Instant.now(), metadata != null ? metadata : Map.of());
    }
}
