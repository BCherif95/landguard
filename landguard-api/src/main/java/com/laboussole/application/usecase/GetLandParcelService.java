package com.laboussole.application.usecase;

import com.laboussole.domain.exception.ParcelNotFoundException;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.in.GetLandParcelUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetLandParcelService implements GetLandParcelUseCase {

    private final LandParcelRepository repository;

    public GetLandParcelService(LandParcelRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public LandParcel execute(ParcelId id) {
        return repository.findById(id).orElseThrow(() -> new ParcelNotFoundException(id));
    }
}
