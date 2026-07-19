package com.laboussole.infrastructure.satellite;

import com.laboussole.domain.model.parcel.ParcelGeometry;

/**
 * WGS84 bounding box of a parcel, padded so the surrounding terrain stays
 * visible on the acquired scene (context matters when judging an intrusion).
 */
public record BoundingBox(double west, double south, double east, double north) {

    public static BoundingBox of(ParcelGeometry geometry, double paddingRatio) {
        double west = Double.POSITIVE_INFINITY;
        double south = Double.POSITIVE_INFINITY;
        double east = Double.NEGATIVE_INFINITY;
        double north = Double.NEGATIVE_INFINITY;
        for (ParcelGeometry.Coordinate point : geometry.outerRing()) {
            west = Math.min(west, point.longitude());
            east = Math.max(east, point.longitude());
            south = Math.min(south, point.latitude());
            north = Math.max(north, point.latitude());
        }
        double lonPadding = (east - west) * paddingRatio;
        double latPadding = (north - south) * paddingRatio;
        return new BoundingBox(
                west - lonPadding, south - latPadding,
                east + lonPadding, north + latPadding);
    }

    /** Height in pixels preserving the bbox aspect ratio for a given width. */
    public int heightFor(int widthPx) {
        double width = east - west;
        double height = north - south;
        if (width <= 0 || height <= 0) {
            return widthPx;
        }
        return Math.max(1, (int) Math.round(widthPx * height / width));
    }
}
