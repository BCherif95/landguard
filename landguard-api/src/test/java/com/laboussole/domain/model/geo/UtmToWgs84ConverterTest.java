package com.laboussole.domain.model.geo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class UtmToWgs84ConverterTest {

    /**
     * Ground-truth pairs generated with pyproj 3.7 (PROJ) against
     * EPSG:32629 / EPSG:32630 — authoritative WGS-84 UTM definitions.
     */
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "Bamako, 29, 608285.921, 1397442.943, 12.6392, -8.0029",
            "Kati,   29, 600717.981, 1409016.884, 12.7441, -8.0722",
            "Dire,   30, 458983.426, 1799979.527, 16.2803, -3.3839",
    })
    void convertsKnownMalianControlPointsWithinOneMetre(
            String city, int zone, double easting, double northing,
            double expectedLat, double expectedLon) {
        var utm = new UtmCoordinate(easting, northing, zone, UtmCoordinate.Hemisphere.NORTH);

        var result = UtmToWgs84Converter.convert(utm);

        double latErrorMetres = Math.abs(result.latitude() - expectedLat) * METRES_PER_DEGREE_LAT;
        double lonErrorMetres = Math.abs(result.longitude() - expectedLon)
                * METRES_PER_DEGREE_LAT * Math.cos(Math.toRadians(expectedLat));

        assertThat(latErrorMetres)
                .as("%s latitude error (metres)", city)
                .isLessThan(1.0);
        assertThat(lonErrorMetres)
                .as("%s longitude error (metres)", city)
                .isLessThan(1.0);
    }

    /** Slightly above the true value (~110 574 m) so the tolerance check is stricter, not looser. */
    private static final double METRES_PER_DEGREE_LAT = 111_320;
}
