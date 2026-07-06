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
        return switch (query.requesterRole()) {
            // Registry-wide read access is an explicit decision: these roles need the
            // full registry for verification, legal review, and platform operations.
            case OFFICER, LEGAL, ADMIN -> repository.findAll(query.limit());
            case CITIZEN -> repository.findByOwner(query.requesterId(), query.limit());
            // Known limitation: BANKER should only see parcels explicitly shared with
            // them for collateral evaluation. That sharing mechanism does not exist yet,
            // so BANKER is restricted to owner scope until it is built.
            case BANKER -> repository.findByOwner(query.requesterId(), query.limit());
        };
    }
}
