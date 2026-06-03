package com.laboussole.application.usecase.legal;

import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.in.legal.GenerateLegalProofUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.SuccessionRepository;
import com.laboussole.domain.port.out.heritage.SuccessionAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateLegalProofService implements GenerateLegalProofUseCase {

    private final LandParcelRepository landParcelRepository;
    private final SuccessionRepository successionRepository;
    private final SuccessionAuditEventRepository auditEventRepository;

    @Override
    @Transactional(readOnly = true)
    public LegalDossier generate(ParcelId parcelId) {
        LandParcel parcel = landParcelRepository.findById(parcelId)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found: " + parcelId));

        SuccessionPlan plan = successionRepository.findByParcelId(parcelId)
                .orElse(null);

        List<com.laboussole.domain.model.heritage.Heir> heirs = List.of();
        List<com.laboussole.domain.model.heritage.SuccessionAuditEvent> auditTrail = List.of();
        String blockchainHash = null;

        if (plan != null) {
            heirs = plan.heirs();
            auditTrail = auditEventRepository.findBySuccessionPlanId(plan.id());
            blockchainHash = plan.blockchainHash();
        }

        String qrCodeContent = String.format("LANDGUARD-VERIFY:%s:%s", parcelId, blockchainHash != null ? blockchainHash : "NOT-ANCHORED");

        return new LegalDossier(
                parcel,
                heirs,
                auditTrail,
                blockchainHash,
                Instant.now(),
                qrCodeContent
        );
    }
}
