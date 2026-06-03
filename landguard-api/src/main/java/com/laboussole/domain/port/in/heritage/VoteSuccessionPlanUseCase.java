package com.laboussole.domain.port.in.heritage;

import com.laboussole.domain.model.heritage.HeirId;
import com.laboussole.domain.model.heritage.SuccessionPlanId;

public interface VoteSuccessionPlanUseCase {
    void vote(SuccessionPlanId planId, HeirId heirId, boolean approved, String ipAddress);
}
