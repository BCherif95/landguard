package com.laboussole.domain.exception;

import com.laboussole.domain.model.parcel.ParcelId;

public final class ParcelNotFoundException extends DomainException {

    public ParcelNotFoundException(ParcelId id) {
        super("PARCEL_NOT_FOUND", "Parcel not found: " + id.asString());
    }
}
