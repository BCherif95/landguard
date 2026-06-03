package com.laboussole.application.usecase.heritage;

import com.laboussole.application.service.ParcelScoringService;
import com.laboussole.domain.model.heritage.SuccessionAuditEvent;
import com.laboussole.domain.model.heritage.SuccessionAuditType;
import com.laboussole.domain.model.heritage.SuccessionPlanId;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.blockchain.BlockchainRecord;
import com.laboussole.domain.port.in.heritage.AnchorSuccessionUseCase;
import com.laboussole.domain.port.out.BlockchainService;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.SuccessionRepository;
import com.laboussole.domain.port.out.heritage.SuccessionAuditEventRepository;
import com.laboussole.domain.port.out.blockchain.BlockchainRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AnchorSuccessionService implements AnchorSuccessionUseCase {
    private final SuccessionRepository repository;
    private final BlockchainService blockchainService;
    private final LandParcelRepository landParcelRepository;
    private final ParcelScoringService scoringService;
    private final SuccessionAuditEventRepository auditEventRepository;
    private final BlockchainRecordRepository blockchainRecordRepository;

    @Override
    @Transactional
    public CompletableFuture<String> execute(SuccessionPlanId planId) {
        return repository.findById(planId)
                .map(plan -> blockchainService.anchor(plan)
                        .thenApply(hash -> {
                            plan.anchorOnBlockchain(hash);
                            repository.save(plan);
                            
                            LandParcel parcel = landParcelRepository.findById(plan.parcelId()).orElse(null);
                            if (parcel != null) {
                                scoringService.updateScores(parcel);
                                landParcelRepository.save(parcel);
                            }

                            blockchainRecordRepository.save(BlockchainRecord.create(
                                    "SUCCESSION_PLAN",
                                    planId.value().toString(),
                                    hash,
                                    null,
                                    "LANDGUARD_SIDECHAIN",
                                    "tx_" + UUID.randomUUID().toString().substring(0, 8)
                            ));

                            auditEventRepository.save(SuccessionAuditEvent.record(
                                    planId,
                                    SuccessionAuditType.BLOCKCHAIN_ANCHORED,
                                    "SYSTEM",
                                    Map.of("hash", hash)
                            ));
                            
                            return hash;
                        }))
                .orElse(CompletableFuture.failedFuture(new IllegalArgumentException("Succession plan not found")));
    }
}
