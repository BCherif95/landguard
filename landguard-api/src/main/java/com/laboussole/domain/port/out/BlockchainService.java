package com.laboussole.domain.port.out;

import com.laboussole.domain.model.heritage.SuccessionPlan;

import java.util.concurrent.CompletableFuture;

public interface BlockchainService {
    CompletableFuture<String> anchor(SuccessionPlan plan);
}
