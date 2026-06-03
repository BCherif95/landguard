package com.laboussole.interfaces.rest.parcel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record RegisterParcelRequest(
        @NotBlank @Size(max = 64) String reference,
        @NotBlank @Size(min = 2, max = 200) String name,
        @NotBlank @Size(min = 2, max = 200) String regionLabel,
        @NotBlank @Size(min = 2, max = 200) String ownerLabel,
        @NotNull @DecimalMin("0.0") BigDecimal areaHectares,
        @PositiveOrZero long estimatedValueXof,
        @NotNull @Valid PolygonDto geometry,
        List<DocumentDto> documents) {
}
