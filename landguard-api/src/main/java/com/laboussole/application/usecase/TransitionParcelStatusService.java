package com.laboussole.application.usecase;

import com.laboussole.domain.exception.ParcelNotFoundException;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.port.in.TransitionParcelStatusUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransitionParcelStatusService implements TransitionParcelStatusUseCase {

    private final LandParcelRepository repository;

    public TransitionParcelStatusService(LandParcelRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public LandParcel execute(Command command) {
        var parcel = repository.findById(command.parcelId())
                .orElseThrow(() -> new ParcelNotFoundException(command.parcelId()));
        parcel.transitionTo(command.targetStatus());
        return repository.save(parcel);
    }
}
