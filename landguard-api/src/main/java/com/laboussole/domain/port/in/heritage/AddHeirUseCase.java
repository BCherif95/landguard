package com.laboussole.domain.port.in.heritage;

import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.model.heritage.SuccessionPlanId;

public interface AddHeirUseCase {
    SuccessionPlan execute(Command command);

    record Command(
            SuccessionPlanId planId,
            String fullName,
            String relation,
            int sharePercentage
    ) {}
}
