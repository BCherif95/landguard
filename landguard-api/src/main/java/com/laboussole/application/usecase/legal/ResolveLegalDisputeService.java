package com.laboussole.application.usecase.legal;

import com.laboussole.application.service.ParcelScoringService;
import com.laboussole.domain.model.legal.LegalDispute;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.port.in.legal.ResolveLegalDisputeUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.legal.LegalDisputeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResolveLegalDisputeService implements ResolveLegalDisputeUseCase {

    private final LegalDisputeRepository disputeRepository;
    private final LandParcelRepository landParcelRepository;
    private final ParcelScoringService scoringService;

    @Override
    @Transactional
    public void resolve(UUID disputeId) {
        LegalDispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found: " + disputeId));

        dispute.resolve();
        disputeRepository.save(dispute);

        LandParcel parcel = landParcelRepository.findById(dispute.parcelId())
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found: " + dispute.parcelId()));

        // Check if there are other open disputes for this parcel
        boolean hasOtherOpenDisputes = disputeRepository.findByParcelId(dispute.parcelId()).stream()
                .anyMatch(d -> d.status() != com.laboussole.domain.model.legal.DisputeStatus.RESOLVED && 
                               d.status() != com.laboussole.domain.model.legal.DisputeStatus.CLOSED);

        if (!hasOtherOpenDisputes) {
            parcel.verifyAndCertify(parcel.trustScore()); // Back to certified if no more disputes
        }
        
        scoringService.updateScores(parcel);
        landParcelRepository.save(parcel);
    }
}
