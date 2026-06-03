package com.laboussole.domain.port.out;

import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.model.heritage.SuccessionPlanId;
import com.laboussole.domain.model.parcel.ParcelId;

import java.util.List;
import java.util.Optional;

public interface SuccessionRepository {
    SuccessionPlan save(SuccessionPlan plan);
    Optional<SuccessionPlan> findById(SuccessionPlanId id);
    Optional<SuccessionPlan> findByParcelId(ParcelId parcelId);
    List<SuccessionPlan> findAll();
}
