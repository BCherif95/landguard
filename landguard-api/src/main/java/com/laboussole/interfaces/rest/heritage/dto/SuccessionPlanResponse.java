package com.laboussole.interfaces.rest.heritage.dto;

import com.laboussole.domain.model.heritage.Heir;
import com.laboussole.domain.model.heritage.SuccessionPlan;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SuccessionPlanResponse(
        UUID id,
        UUID parcelId,
        String status,
        String blockchainHash,
        List<HeirResponse> heirs,
        Instant createdAt,
        Instant updatedAt
) {
    public static SuccessionPlanResponse from(SuccessionPlan plan) {
        return new SuccessionPlanResponse(
                plan.id().value(),
                plan.parcelId().value(),
                plan.status().name(),
                plan.blockchainHash(),
                plan.heirs().stream().map(HeirResponse::from).toList(),
                plan.createdAt(),
                plan.updatedAt()
        );
    }
}
