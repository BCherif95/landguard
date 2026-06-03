package com.laboussole.application.usecase;

import com.laboussole.domain.exception.ParcelNotFoundException;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.port.in.VerifyLandParcelUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerifyLandParcelService implements VerifyLandParcelUseCase {

    private final LandParcelRepository repository;

    public VerifyLandParcelService(LandParcelRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public LandParcel execute(Command command) {
        var parcel = repository.findById(command.parcelId())
                .orElseThrow(() -> new ParcelNotFoundException(command.parcelId()));
        parcel.transitionTo(com.laboussole.domain.model.parcel.ParcelStatus.CERTIFIED);
        parcel.updateTrustScore(command.newTrustScore());
        return repository.save(parcel);
    }
}
