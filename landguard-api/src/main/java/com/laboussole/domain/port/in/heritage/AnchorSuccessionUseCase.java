package com.laboussole.domain.port.in.heritage;

import com.laboussole.domain.model.heritage.SuccessionPlanId;

public interface AnchorSuccessionUseCase {

    /** Seals the plan on the ledger and returns the anchored hash. */
    String execute(SuccessionPlanId planId);
}
