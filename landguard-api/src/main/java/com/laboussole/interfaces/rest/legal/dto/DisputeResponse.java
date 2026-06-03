package com.laboussole.interfaces.rest.legal.dto;

import com.laboussole.domain.model.legal.DisputeSeverity;
import com.laboussole.domain.model.legal.DisputeStatus;
import com.laboussole.domain.model.legal.LegalDispute;

import java.time.Instant;
import java.util.UUID;

public record DisputeResponse(
        UUID id,
        UUID parcelId,
        String reason,
        DisputeSeverity severity,
        DisputeStatus status,
        Instant openedAt,
        Instant resolvedAt
) {
    public static DisputeResponse from(LegalDispute dispute) {
        return new DisputeResponse(
                dispute.id(),
                dispute.parcelId().value(),
                dispute.reason(),
                dispute.severity(),
                dispute.status(),
                dispute.openedAt(),
                dispute.resolvedAt()
        );
    }
}
