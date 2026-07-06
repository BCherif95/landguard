package com.laboussole.application.usecase;

import com.laboussole.domain.exception.ParcelNotFoundException;
import com.laboussole.domain.model.parcel.LandParcel;
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
    public LandParcel execute(Query query) {
        var parcel = repository.findById(query.parcelId())
                .orElseThrow(() -> new ParcelNotFoundException(query.parcelId()));
        if (!isVisibleTo(parcel, query)) {
            // 404 instead of 403 so a requester cannot probe which parcel ids exist.
            throw new ParcelNotFoundException(query.parcelId());
        }
        return parcel;
    }

    private static boolean isVisibleTo(LandParcel parcel, Query query) {
        return switch (query.requesterRole()) {
            case OFFICER, LEGAL, ADMIN -> true;
            // BANKER: same owner-only scope as CITIZEN until an explicit
            // parcel-sharing mechanism for collateral evaluation exists.
            case CITIZEN, BANKER ->
                    parcel.ownerUserId() != null && parcel.ownerUserId().equals(query.requesterId());
        };
    }
}
