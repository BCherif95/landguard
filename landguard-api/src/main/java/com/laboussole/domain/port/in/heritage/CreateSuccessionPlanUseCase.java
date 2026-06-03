package com.laboussole.domain.port.in.heritage;

import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.model.parcel.ParcelId;

public interface CreateSuccessionPlanUseCase {
    SuccessionPlan execute(ParcelId parcelId);
}
