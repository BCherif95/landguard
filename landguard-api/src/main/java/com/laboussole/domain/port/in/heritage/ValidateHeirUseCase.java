package com.laboussole.domain.port.in.heritage;

import com.laboussole.domain.model.heritage.HeirId;
import com.laboussole.domain.model.heritage.SuccessionPlanId;

public interface ValidateHeirUseCase {
    void execute(SuccessionPlanId planId, HeirId heirId);
}
