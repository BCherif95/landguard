package com.laboussole.domain.port.in.legal;

import java.util.UUID;

public interface ResolveLegalDisputeUseCase {
    void resolve(UUID disputeId);
}
