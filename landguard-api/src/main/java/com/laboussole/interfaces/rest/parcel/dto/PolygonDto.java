package com.laboussole.interfaces.rest.parcel.dto;

import com.laboussole.domain.model.parcel.ParcelGeometry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * GeoJSON-compatible Polygon.
 * Format: { "type": "Polygon", "coordinates": [ [ [lng, lat], ... ] ] }
 */
public record PolygonDto(
        @NotBlank String type,
        @NotNull @Size(min = 1)
        List<List<List<Double>>> coordinates) {

    public ParcelGeometry toDomain() {
        if (!"Polygon".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("Unsupported geometry type: " + type);
        }
        if (coordinates.isEmpty()) {
            throw new IllegalArgumentException("Polygon must have at least one ring");
        }
        // We only support the outer ring (the first one) in our domain for now
        var outerRingCoords = coordinates.get(0);
        var ring = outerRingCoords.stream()
                .map(p -> new ParcelGeometry.Coordinate(p.get(0), p.get(1)))
                .toList();
        return new ParcelGeometry(ring);
    }

    public static PolygonDto fromDomain(ParcelGeometry geometry) {
        var ring = geometry.outerRing().stream()
                .map(c -> List.of(c.longitude(), c.latitude()))
                .toList();
        return new PolygonDto("Polygon", List.of(ring));
    }
}
