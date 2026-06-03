package com.laboussole.interfaces.rest.heritage.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VoteRequest(
        @NotNull UUID heirId,
        boolean approved
) {}
