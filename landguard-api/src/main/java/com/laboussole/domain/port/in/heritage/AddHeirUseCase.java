package com.laboussole.domain.port.in.heritage;

import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.model.heritage.SuccessionPlanId;

public interface AddHeirUseCase {
    SuccessionPlan execute(Command command);

    /**
     * {@code accountEmail} is optional: when provided and matching a platform
     * account, the heir is linked to it (Feature 04.1) and will receive the
     * shared notifications of the parcel (Feature 04.2).
     */
    record Command(
            SuccessionPlanId planId,
            String fullName,
            String relation,
            int sharePercentage,
            String accountEmail
    ) {}
}
