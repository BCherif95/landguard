package com.laboussole.application.usecase.heritage;

import com.laboussole.application.service.ParcelScoringService;
import com.laboussole.domain.model.heritage.SuccessionAuditEvent;
import com.laboussole.domain.model.heritage.SuccessionAuditType;
import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.model.heritage.SuccessionPlanId;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.port.in.heritage.AnchorSuccessionUseCase;
import com.laboussole.domain.port.out.BlockchainService;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.SuccessionRepository;
import com.laboussole.domain.port.out.heritage.SuccessionAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnchorSuccessionService implements AnchorSuccessionUseCase {

    private static final String ENTITY_TYPE = "SUCCESSION_PLAN";

    private final SuccessionRepository repository;
    private final BlockchainService blockchainService;
    private final LandParcelRepository landParcelRepository;
    private final ParcelScoringService scoringService;
    private final SuccessionAuditEventRepository auditEventRepository;

    @Override
    @Transactional
    public String execute(SuccessionPlanId planId) {
        var plan = repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Succession plan not found"));

        var record = blockchainService.anchor(
                ENTITY_TYPE, planId.value().toString(), canonicalPayload(plan));

        plan.anchorOnBlockchain(record.hash());
        repository.save(plan);

        LandParcel parcel = landParcelRepository.findById(plan.parcelId()).orElse(null);
        if (parcel != null) {
            scoringService.updateScores(parcel);
            landParcelRepository.save(parcel);
        }

        auditEventRepository.save(SuccessionAuditEvent.record(
                planId,
                SuccessionAuditType.BLOCKCHAIN_ANCHORED,
                "SYSTEM",
                Map.of("hash", record.hash(), "transactionId", record.transactionId())
        ));

        return record.hash();
    }

    /**
     * Deterministic representation of the plan's legally meaningful state.
     * Heirs are sorted by id so the payload — and therefore the seal — does not
     * depend on insertion order.
     */
    private String canonicalPayload(SuccessionPlan plan) {
        var heirs = plan.heirs().stream()
                .sorted(Comparator.comparing(h -> h.id().value().toString()))
                .map(h -> String.join("|",
                        h.id().value().toString(),
                        h.fullName(),
                        h.relation(),
                        String.valueOf(h.sharePercentage()),
                        String.valueOf(h.validated())))
                .collect(Collectors.joining(";"));
        return String.join("\n",
                "plan=" + plan.id().value(),
                "parcel=" + plan.parcelId().value(),
                "status=" + plan.status(),
                "heirs=" + heirs);
    }
}
