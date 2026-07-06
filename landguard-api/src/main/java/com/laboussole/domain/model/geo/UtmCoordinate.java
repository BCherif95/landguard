package com.laboussole.domain.model.geo;

import com.laboussole.domain.exception.InvalidUtmCoordinateException;

/**
 * A projected UTM position restricted to the zones covering Malian territory
 * (29N and 30N, matching the "plan de situation" format used by Malian land
 * surveys). Validation messages are user-facing French by design — they are
 * returned verbatim to the form that submitted the coordinates.
 *
 * <p>Accepted ranges:
 * <ul>
 *   <li>easting — standard UTM validity band [166 000, 834 000] m;</li>
 *   <li>northing — [1 100 000, 2 800 000] m, i.e. roughly latitudes 10°N to
 *       25°N which bound Mali;</li>
 *   <li>zone — 29 or 30, northern hemisphere only.</li>
 * </ul>
 */
public record UtmCoordinate(double easting, double northing, int zoneNumber, Hemisphere hemisphere) {

    public enum Hemisphere { NORTH, SOUTH }

    public static final double MIN_EASTING = 166_000;
    public static final double MAX_EASTING = 834_000;
    public static final double MIN_NORTHING = 1_100_000;
    public static final double MAX_NORTHING = 2_800_000;

    public UtmCoordinate {
        if (zoneNumber != 29 && zoneNumber != 30 || hemisphere != Hemisphere.NORTH) {
            throw new InvalidUtmCoordinateException(
                    "La zone UTM doit être 29N ou 30N pour le territoire malien.");
        }
        if (!Double.isFinite(easting) || easting < MIN_EASTING || easting > MAX_EASTING) {
            throw new InvalidUtmCoordinateException(
                    "La coordonnée Est (Easting) doit être comprise entre 166 000 et 834 000 mètres.");
        }
        if (!Double.isFinite(northing) || northing < MIN_NORTHING || northing > MAX_NORTHING) {
            throw new InvalidUtmCoordinateException(
                    "La coordonnée Nord (Northing) doit être comprise entre 1 100 000 et 2 800 000 mètres"
                            + " pour le territoire malien.");
        }
    }

    /**
     * Builds a coordinate from a zone label as printed on Malian situation
     * plans, e.g. {@code "29N"} or {@code "30N"} (case-insensitive).
     */
    public static UtmCoordinate of(double easting, double northing, String zoneLabel) {
        if (zoneLabel == null) {
            throw new InvalidUtmCoordinateException(
                    "La zone UTM doit être 29N ou 30N pour le territoire malien.");
        }
        var normalised = zoneLabel.trim().toUpperCase();
        return switch (normalised) {
            case "29N" -> new UtmCoordinate(easting, northing, 29, Hemisphere.NORTH);
            case "30N" -> new UtmCoordinate(easting, northing, 30, Hemisphere.NORTH);
            default -> throw new InvalidUtmCoordinateException(
                    "La zone UTM doit être 29N ou 30N pour le territoire malien.");
        };
    }
}
