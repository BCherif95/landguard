package com.laboussole.interfaces.rest.legal.dto;

import com.laboussole.domain.model.legal.DisputeSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OpenDisputeRequest(
        @NotNull UUID parcelId,
        @NotBlank String reason,
        @NotNull DisputeSeverity severity
) {}
