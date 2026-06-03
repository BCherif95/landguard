package com.laboussole.application.usecase.legal;

import com.laboussole.domain.model.heritage.Heir;
import com.laboussole.domain.model.heritage.SuccessionAuditEvent;
import com.laboussole.domain.model.parcel.LandParcel;

import java.time.Instant;
import java.util.List;

public record LegalDossier(
        LandParcel parcel,
        List<Heir> heirs,
        List<SuccessionAuditEvent> auditTrail,
        String blockchainHash,
        Instant generatedAt,
        String qrCodeContent
) {}
