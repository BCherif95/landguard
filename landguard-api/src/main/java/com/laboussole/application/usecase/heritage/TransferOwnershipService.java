package com.laboussole.application.usecase.heritage;

import com.laboussole.domain.model.heritage.*;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.port.in.heritage.TransferOwnershipUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.SuccessionRepository;
import com.laboussole.domain.port.out.heritage.SuccessionAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferOwnershipService implements TransferOwnershipUseCase {

    private final SuccessionRepository successionRepository;
    private final LandParcelRepository landParcelRepository;
    private final SuccessionAuditEventRepository auditEventRepository;

    @Override
    @Transactional
    public void transfer(SuccessionPlanId planId, String actor) {
        SuccessionPlan plan = successionRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Succession plan not found: " + planId));

        if (plan.status() != SuccessionStatus.ANCHORED) {
            throw new IllegalStateException("Can only transfer ownership for ANCHORED plans. Current status: " + plan.status());
        }

        LandParcel parcel = landParcelRepository.findById(plan.parcelId())
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found: " + plan.parcelId()));

        String newOwnerLabel = plan.heirs().stream()
                .map(heir -> heir.fullName() + " (" + heir.sharePercentage() + "%)")
                .collect(Collectors.joining(", "));

        parcel.transferOwnership(newOwnerLabel);
        landParcelRepository.save(parcel);

        plan.markAsTransferred();
        successionRepository.save(plan);

        SuccessionAuditEvent event = SuccessionAuditEvent.record(
                planId,
                SuccessionAuditType.OWNERSHIP_TRANSFERRED,
                actor,
                Map.of("newOwner", newOwnerLabel)
        );
        auditEventRepository.save(event);
    }
}
