package com.laboussole.interfaces.rest.geo.dto;

import jakarta.validation.constraints.NotNull;

/** One polygon vertex expressed in UTM metres. */
public record UtmPointDto(
        @NotNull(message = "La coordonnée Est (Easting) est requise.") Double easting,
        @NotNull(message = "La coordonnée Nord (Northing) est requise.") Double northing) {
}
