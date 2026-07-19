package com.laboussole.application.usecase.legal;

import com.laboussole.domain.model.heritage.Heir;
import com.laboussole.domain.model.heritage.SuccessionAuditEvent;
import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.monitoring.SatelliteSnapshot;
import com.laboussole.domain.model.parcel.LandParcel;

import java.time.Instant;
import java.util.List;

/**
 * Court-ready evidence bundle (PRD Feature 03.3): certified owner identity,
 * exact geographic coordinates, the monitoring event chronology and the
 * before/after satellite snapshots with their certified timestamps.
 */
public record LegalDossier(
        LandParcel parcel,
        String ownerEmail,
        List<Heir> heirs,
        List<SuccessionAuditEvent> auditTrail,
        List<MonitoringEvent> monitoringEvents,
        List<SatelliteSnapshot> snapshots,
        String blockchainHash,
        Instant generatedAt,
        String qrCodeContent
) {}
