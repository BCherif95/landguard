package com.laboussole.application.usecase.legal;

import com.laboussole.domain.model.heritage.Heir;
import com.laboussole.domain.model.heritage.SuccessionAuditEvent;
import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.model.monitoring.SatelliteSnapshot;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.model.User;
import com.laboussole.domain.port.in.legal.GenerateLegalProofUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.MonitoringRepository;
import com.laboussole.domain.port.out.SatelliteSnapshotRepository;
import com.laboussole.domain.port.out.SuccessionRepository;
import com.laboussole.domain.port.out.UserRepository;
import com.laboussole.domain.port.out.heritage.SuccessionAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GenerateLegalProofService implements GenerateLegalProofUseCase {

    private final LandParcelRepository landParcelRepository;
    private final SuccessionRepository successionRepository;
    private final SuccessionAuditEventRepository auditEventRepository;
    private final MonitoringRepository monitoringRepository;
    private final SatelliteSnapshotRepository snapshotRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public LegalDossier generate(ParcelId parcelId) {
        LandParcel parcel = landParcelRepository.findById(parcelId)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found: " + parcelId));

        SuccessionPlan plan = successionRepository.findByParcelId(parcelId).orElse(null);

        List<Heir> heirs = List.of();
        List<SuccessionAuditEvent> auditTrail = List.of();
        String blockchainHash = null;
        if (plan != null) {
            heirs = plan.heirs();
            auditTrail = auditEventRepository.findBySuccessionPlanId(plan.id());
            blockchainHash = plan.blockchainHash();
        }

        var events = monitoringRepository.findByParcelId(parcelId).stream()
                .sorted(Comparator.comparing(e -> e.detectedAt()))
                .toList();
        var snapshots = snapshotRepository.findByParcelId(parcelId).stream()
                .sorted(Comparator.comparing(SatelliteSnapshot::capturedAt))
                .toList();

        String ownerEmail = Optional.ofNullable(parcel.ownerUserId())
                .flatMap(userRepository::findById)
                .map(User::email)
                .map(e -> e.value())
                .orElse(null);

        String qrCodeContent = String.format("LANDGUARD-VERIFY:%s:%s",
                parcelId.value(), blockchainHash != null ? blockchainHash : "NOT-ANCHORED");

        return new LegalDossier(
                parcel,
                ownerEmail,
                heirs,
                auditTrail,
                events,
                snapshots,
                blockchainHash,
                Instant.now(),
                qrCodeContent
        );
    }
}
