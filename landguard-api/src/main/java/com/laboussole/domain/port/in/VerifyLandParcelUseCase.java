package com.laboussole.domain.port.in;

import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.ParcelId;

public interface VerifyLandParcelUseCase {

    LandParcel execute(Command command);

    record Command(ParcelId parcelId, int newTrustScore) {}
}
