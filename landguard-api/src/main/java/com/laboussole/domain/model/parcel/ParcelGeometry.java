package com.laboussole.domain.model.parcel;

import java.util.List;
import java.util.Objects;

/**
 * Domain-level representation of a parcel's outer boundary. Coordinates are
 * (longitude, latitude) pairs in WGS-84 (SRID 4326). The first and last point
 * MUST coincide (closed ring) — enforced in the compact constructor.
 *
 * <p>Kept as a plain record so the domain has zero dependency on JTS / Hibernate
 * Spatial — the conversion happens in the persistence adapter.
 */
public record ParcelGeometry(List<Coordinate> outerRing) {

    public ParcelGeometry {
        Objects.requireNonNull(outerRing, "outerRing must not be null");
        if (outerRing.size() < 4) {
            throw new IllegalArgumentException("Polygon outer ring must have at least 4 points");
        }
        var first = outerRing.get(0);
        var last = outerRing.get(outerRing.size() - 1);
        if (!first.equals(last)) {
            throw new IllegalArgumentException("Polygon outer ring must be closed (first == last)");
        }
        outerRing = List.copyOf(outerRing);
    }

    /** WGS-84 lon/lat pair. */
    public record Coordinate(double longitude, double latitude) {
        public Coordinate {
            if (longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException("Longitude out of range: " + longitude);
            }
            if (latitude < -90 || latitude > 90) {
                throw new IllegalArgumentException("Latitude out of range: " + latitude);
            }
        }
    }
}
