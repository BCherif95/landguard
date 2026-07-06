package com.laboussole.interfaces.rest.geo.dto;

import com.laboussole.domain.model.geo.Wgs84Point;

import java.util.List;

/**
 * GeoJSON Polygon (RFC 7946): coordinates are [longitude, latitude] pairs,
 * outer ring closed. Same shape as the geometry consumed by the map client.
 */
public record GeoJsonPolygonResponse(
        String type,
        List<List<List<Double>>> coordinates) {

    public static GeoJsonPolygonResponse from(List<Wgs84Point> closedRing) {
        var ring = closedRing.stream()
                .map(p -> List.of(p.longitude(), p.latitude()))
                .toList();
        return new GeoJsonPolygonResponse("Polygon", List.of(ring));
    }
}
