package com.laboussole.domain.port.in.heritage;

import com.laboussole.domain.model.heritage.SuccessionPlanId;

public interface SubmitSuccessionForVotingUseCase {
    void submit(SuccessionPlanId planId, String actor);
}
