package com.laboussole.interfaces.rest.geo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Polygon captured from a Malian situation plan: a single UTM zone label
 * ({@code "29N"} or {@code "30N"}) shared by every vertex.
 */
public record UtmToGeoJsonRequest(
        @NotBlank(message = "La zone UTM est requise.") String zone,
        @NotNull(message = "La liste des sommets est requise.")
        @Size(min = 3, message = "Un polygone requiert au moins 3 sommets.")
        List<@Valid UtmPointDto> points) {
}
