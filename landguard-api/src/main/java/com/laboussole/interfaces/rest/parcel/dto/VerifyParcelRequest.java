package com.laboussole.interfaces.rest.parcel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record VerifyParcelRequest(
        @Min(0) @Max(100) int newTrustScore) {
}
