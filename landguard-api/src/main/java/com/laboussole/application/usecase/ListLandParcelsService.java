package com.laboussole.application.usecase;

import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.port.in.ListLandParcelsUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListLandParcelsService implements ListLandParcelsUseCase {

    private final LandParcelRepository repository;

    public ListLandParcelsService(LandParcelRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LandParcel> execute(Query query) {
        return query.ownerUserId()
                .map(owner -> repository.findByOwner(owner, query.limit()))
                .orElseGet(() -> repository.findAll(query.limit()));
    }
}
