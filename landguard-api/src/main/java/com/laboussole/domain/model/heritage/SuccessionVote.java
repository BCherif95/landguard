package com.laboussole.domain.model.heritage;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record SuccessionVote(
        UUID id,
        SuccessionPlanId successionPlanId,
        HeirId heirId,
        boolean approved,
        Instant votedAt,
        String ipAddress
) {
    public SuccessionVote {
        Objects.requireNonNull(id);
        Objects.requireNonNull(successionPlanId);
        Objects.requireNonNull(heirId);
        Objects.requireNonNull(votedAt);
    }

    public static SuccessionVote cast(SuccessionPlanId planId, HeirId heirId, boolean approved, String ipAddress) {
        return new SuccessionVote(UUID.randomUUID(), planId, heirId, approved, Instant.now(), ipAddress);
    }
}
