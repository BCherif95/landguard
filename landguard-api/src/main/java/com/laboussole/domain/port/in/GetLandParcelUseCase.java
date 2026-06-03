package com.laboussole.domain.port.in;

import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.ParcelId;

public interface GetLandParcelUseCase {

    LandParcel execute(ParcelId id);
}
