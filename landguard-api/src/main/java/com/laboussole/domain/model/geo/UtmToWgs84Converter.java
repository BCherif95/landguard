package com.laboussole.domain.model.geo;

/**
 * Pure inverse Transverse Mercator projection (UTM → WGS-84 geographic),
 * framework-free so it can live in the domain layer.
 *
 * <p>Implements the classic USGS series expansion (Snyder, "Map Projections —
 * A Working Manual", 1987, formulas 8-17…8-25 / 10-15…10-22) on the WGS-84
 * ellipsoid. Accuracy is sub-centimetre inside a UTM zone, far below the
 * ±1 m tolerance required by the Malian situation-plan workflow.
 */
public final class UtmToWgs84Converter {

    /** WGS-84 semi-major axis (metres). */
    private static final double SEMI_MAJOR_AXIS = 6_378_137.0;
    /** WGS-84 flattening. */
    private static final double FLATTENING = 1 / 298.257223563;
    /** UTM central-meridian scale factor. */
    private static final double SCALE_FACTOR = 0.9996;
    private static final double FALSE_EASTING = 500_000.0;
    private static final double FALSE_NORTHING_SOUTH = 10_000_000.0;

    private static final double E2 = FLATTENING * (2 - FLATTENING);          // first eccentricity squared
    private static final double EP2 = E2 / (1 - E2);                         // second eccentricity squared
    private static final double E1 = (1 - Math.sqrt(1 - E2)) / (1 + Math.sqrt(1 - E2));

    private UtmToWgs84Converter() {
    }

    public static Wgs84Point convert(UtmCoordinate utm) {
        double x = utm.easting() - FALSE_EASTING;
        double y = utm.hemisphere() == UtmCoordinate.Hemisphere.SOUTH
                ? utm.northing() - FALSE_NORTHING_SOUTH
                : utm.northing();

        // Footpoint latitude from the rectifying meridian arc.
        double meridianArc = y / SCALE_FACTOR;
        double mu = meridianArc
                / (SEMI_MAJOR_AXIS * (1 - E2 / 4 - 3 * E2 * E2 / 64 - 5 * E2 * E2 * E2 / 256));
        double phi1 = mu
                + (3 * E1 / 2 - 27 * Math.pow(E1, 3) / 32) * Math.sin(2 * mu)
                + (21 * E1 * E1 / 16 - 55 * Math.pow(E1, 4) / 32) * Math.sin(4 * mu)
                + (151 * Math.pow(E1, 3) / 96) * Math.sin(6 * mu)
                + (1097 * Math.pow(E1, 4) / 512) * Math.sin(8 * mu);

        double sinPhi1 = Math.sin(phi1);
        double cosPhi1 = Math.cos(phi1);
        double tanPhi1 = Math.tan(phi1);

        double c1 = EP2 * cosPhi1 * cosPhi1;
        double t1 = tanPhi1 * tanPhi1;
        double n1 = SEMI_MAJOR_AXIS / Math.sqrt(1 - E2 * sinPhi1 * sinPhi1);
        double r1 = SEMI_MAJOR_AXIS * (1 - E2) / Math.pow(1 - E2 * sinPhi1 * sinPhi1, 1.5);
        double d = x / (n1 * SCALE_FACTOR);

        double latitude = phi1 - (n1 * tanPhi1 / r1) * (
                d * d / 2
                        - (5 + 3 * t1 + 10 * c1 - 4 * c1 * c1 - 9 * EP2) * Math.pow(d, 4) / 24
                        + (61 + 90 * t1 + 298 * c1 + 45 * t1 * t1 - 252 * EP2 - 3 * c1 * c1)
                                * Math.pow(d, 6) / 720);

        double centralMeridian = Math.toRadians((utm.zoneNumber() - 1) * 6 - 180 + 3);
        double longitude = centralMeridian + (
                d
                        - (1 + 2 * t1 + c1) * Math.pow(d, 3) / 6
                        + (5 - 2 * c1 + 28 * t1 - 3 * c1 * c1 + 8 * EP2 + 24 * t1 * t1)
                                * Math.pow(d, 5) / 120)
                / cosPhi1;

        return new Wgs84Point(Math.toDegrees(latitude), Math.toDegrees(longitude));
    }
}
