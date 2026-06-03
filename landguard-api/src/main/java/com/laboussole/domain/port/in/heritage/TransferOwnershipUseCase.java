package com.laboussole.domain.port.in.heritage;

import com.laboussole.domain.model.heritage.SuccessionPlanId;

public interface TransferOwnershipUseCase {
    void transfer(SuccessionPlanId planId, String actor);
}
