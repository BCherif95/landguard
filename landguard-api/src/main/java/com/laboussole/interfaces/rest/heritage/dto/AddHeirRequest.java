package com.laboussole.interfaces.rest.heritage.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddHeirRequest(
        @NotBlank String fullName,
        @NotBlank String relation,
        @Min(1) @Max(100) int sharePercentage,
        @Email String accountEmail
) {}
