package com.laboussole.domain.port.in.heritage;

import com.laboussole.domain.model.heritage.SuccessionPlanId;

import java.util.concurrent.CompletableFuture;

public interface AnchorSuccessionUseCase {
    CompletableFuture<String> execute(SuccessionPlanId planId);
}
