package com.laboussole.domain.port.in;

import com.laboussole.domain.model.parcel.ParcelId;

public interface CalculateMovementScoreUseCase {
    int execute(ParcelId parcelId);
}
