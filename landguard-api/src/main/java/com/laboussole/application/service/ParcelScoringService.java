package com.laboussole.application.service;

import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.model.heritage.SuccessionStatus;
import com.laboussole.domain.model.legal.LegalDispute;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.port.out.SuccessionRepository;
import com.laboussole.domain.port.out.legal.LegalDisputeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParcelScoringService {

    private final LegalDisputeRepository disputeRepository;
    private final SuccessionRepository successionRepository;

    public void updateScores(LandParcel parcel) {
        int baseTrust = parcel.trustScore();
        int baseRisk = parcel.riskScore();

        // 1. Adjust by Disputes
        List<LegalDispute> disputes = disputeRepository.findByParcelId(parcel.id());
        long openDisputes = disputes.stream()
                .filter(d -> d.status() != com.laboussole.domain.model.legal.DisputeStatus.RESOLVED && 
                             d.status() != com.laboussole.domain.model.legal.DisputeStatus.CLOSED)
                .count();

        if (openDisputes > 0) {
            baseTrust -= 20 * openDisputes;
            baseRisk += 30 * openDisputes;
        }

        // 2. Adjust by Succession
        successionRepository.findByParcelId(parcel.id()).ifPresent(plan -> {
            if (plan.status() == SuccessionStatus.ANCHORED || plan.status() == SuccessionStatus.TRANSFERRED) {
                // baseTrust += 15; // Trust increase if anchored
            } else if (plan.status() == SuccessionStatus.REJECTED) {
                // baseTrust -= 10;
            }
        });

        // Ensure scores are in [0, 100]
        int finalTrust = Math.max(0, Math.min(100, baseTrust));
        int finalRisk = Math.max(0, Math.min(100, baseRisk));

        parcel.updateTrustScore(finalTrust);
        parcel.updateRiskScore(finalRisk);
    }
}
