package com.laboussole.application.usecase.legal;

import com.laboussole.application.service.ParcelScoringService;
import com.laboussole.domain.model.legal.DisputeSeverity;
import com.laboussole.domain.model.legal.LegalDispute;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.in.legal.OpenLegalDisputeUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.legal.LegalDisputeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OpenLegalDisputeService implements OpenLegalDisputeUseCase {

    private final LegalDisputeRepository disputeRepository;
    private final LandParcelRepository landParcelRepository;
    private final ParcelScoringService scoringService;

    @Override
    @Transactional
    public UUID open(ParcelId parcelId, String reason, DisputeSeverity severity) {
        LandParcel parcel = landParcelRepository.findById(parcelId)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found: " + parcelId));

        LegalDispute dispute = LegalDispute.open(parcelId, reason, severity);
        disputeRepository.save(dispute);

        parcel.flagDispute();
        scoringService.updateScores(parcel);
        landParcelRepository.save(parcel);

        return dispute.id();
    }
}
