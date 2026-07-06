package com.laboussole.domain.model.geo;

/**
 * A geographic position on the WGS-84 ellipsoid, in decimal degrees.
 * Internal invariant checks only — a violation means a converter bug,
 * never bad user input.
 */
public record Wgs84Point(double latitude, double longitude) {

    public Wgs84Point {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude out of range: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude out of range: " + longitude);
        }
    }
}
