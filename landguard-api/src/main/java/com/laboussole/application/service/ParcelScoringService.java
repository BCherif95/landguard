package com.laboussole.application.service;

import com.laboussole.domain.model.legal.DisputeStatus;
import com.laboussole.domain.model.legal.LegalDispute;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.port.out.legal.LegalDisputeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Recalculates a parcel's trust and risk scores from its current legal situation.
 * Called whenever an event that affects legal standing occurs (dispute opened or
 * resolved, succession anchored). Scores always stay within [0, 100].
 */
@Service
public class ParcelScoringService {

    /** Each unresolved dispute erodes confidence in the ownership claim. */
    private static final int TRUST_PENALTY_PER_OPEN_DISPUTE = 20;

    /** Each unresolved dispute raises the likelihood of a contested transaction. */
    private static final int RISK_INCREASE_PER_OPEN_DISPUTE = 30;

    private final LegalDisputeRepository disputeRepository;

    public ParcelScoringService(LegalDisputeRepository disputeRepository) {
        this.disputeRepository = disputeRepository;
    }

    public void updateScores(LandParcel parcel) {
        long openDisputes = countOpenDisputes(parcel);

        int trust = clamp(parcel.trustScore() - (int) (TRUST_PENALTY_PER_OPEN_DISPUTE * openDisputes));
        int risk = clamp(parcel.riskScore() + (int) (RISK_INCREASE_PER_OPEN_DISPUTE * openDisputes));

        parcel.updateTrustScore(trust);
        parcel.updateRiskScore(risk);
    }

    private long countOpenDisputes(LandParcel parcel) {
        List<LegalDispute> disputes = disputeRepository.findByParcelId(parcel.id());
        return disputes.stream()
                .filter(d -> d.status() != DisputeStatus.RESOLVED && d.status() != DisputeStatus.CLOSED)
                .count();
    }

    private static int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
