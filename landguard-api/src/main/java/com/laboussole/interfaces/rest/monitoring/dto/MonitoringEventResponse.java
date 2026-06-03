package com.laboussole.interfaces.rest.monitoring.dto;

import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.monitoring.MonitoringEventType;
import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import com.laboussole.domain.model.monitoring.MonitoringSource;

import java.time.Instant;
import java.util.UUID;

public record MonitoringEventResponse(
        UUID id,
        UUID parcelId,
        MonitoringEventType type,
        MonitoringSeverity severity,
        Instant detectedAt,
        int confidenceScore,
        double longitude,
        double latitude,
        String description,
        MonitoringSource source,
        String imageUrl,
        boolean resolved
) {
    public static MonitoringEventResponse fromDomain(MonitoringEvent domain) {
        return new MonitoringEventResponse(
                domain.id().value(),
                domain.parcelId().value(),
                domain.type(),
                domain.severity(),
                domain.detectedAt(),
                domain.confidenceScore(),
                domain.coordinates().longitude(),
                domain.coordinates().latitude(),
                domain.description(),
                domain.source(),
                domain.imageUrl(),
                domain.resolved()
        );
    }
}
