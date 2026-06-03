package com.laboussole.domain.port.in;

import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.model.parcel.ParcelStatus;

public interface TransitionParcelStatusUseCase {
    LandParcel execute(Command command);

    record Command(ParcelId parcelId, ParcelStatus targetStatus) {}
}
