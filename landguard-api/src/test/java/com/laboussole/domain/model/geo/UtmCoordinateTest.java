package com.laboussole.domain.model.geo;

import com.laboussole.domain.exception.InvalidUtmCoordinateException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UtmCoordinateTest {

    @Test
    void acceptsValidMalianCoordinate() {
        var coordinate = new UtmCoordinate(608_285.9, 1_397_442.9, 29, UtmCoordinate.Hemisphere.NORTH);

        assertThat(coordinate.zoneNumber()).isEqualTo(29);
        assertThat(coordinate.hemisphere()).isEqualTo(UtmCoordinate.Hemisphere.NORTH);
    }

    @Test
    void parsesZoneLabelCaseInsensitively() {
        var coordinate = UtmCoordinate.of(458_983.4, 1_799_979.5, "30n");

        assertThat(coordinate.zoneNumber()).isEqualTo(30);
        assertThat(coordinate.hemisphere()).isEqualTo(UtmCoordinate.Hemisphere.NORTH);
    }

    @Test
    void rejectsZoneOutsideMali() {
        assertThatThrownBy(() -> new UtmCoordinate(500_000, 1_500_000, 28, UtmCoordinate.Hemisphere.NORTH))
                .isInstanceOf(InvalidUtmCoordinateException.class)
                .hasMessage("La zone UTM doit être 29N ou 30N pour le territoire malien.");
    }

    @Test
    void rejectsSouthernHemisphere() {
        assertThatThrownBy(() -> UtmCoordinate.of(500_000, 1_500_000, "29S"))
                .isInstanceOf(InvalidUtmCoordinateException.class)
                .hasMessage("La zone UTM doit être 29N ou 30N pour le territoire malien.");
    }

    @Test
    void rejectsUnknownZoneLabel() {
        assertThatThrownBy(() -> UtmCoordinate.of(500_000, 1_500_000, "31N"))
                .isInstanceOf(InvalidUtmCoordinateException.class)
                .hasMessage("La zone UTM doit être 29N ou 30N pour le territoire malien.");
    }

    @Test
    void rejectsEastingOutsideUtmValidityBand() {
        assertThatThrownBy(() -> new UtmCoordinate(90_000, 1_500_000, 29, UtmCoordinate.Hemisphere.NORTH))
                .isInstanceOf(InvalidUtmCoordinateException.class)
                .hasMessage("La coordonnée Est (Easting) doit être comprise entre 166 000 et 834 000 mètres.");
    }

    @Test
    void rejectsNorthingOutsideMalianRange() {
        assertThatThrownBy(() -> new UtmCoordinate(500_000, 900_000, 29, UtmCoordinate.Hemisphere.NORTH))
                .isInstanceOf(InvalidUtmCoordinateException.class)
                .hasMessage("La coordonnée Nord (Northing) doit être comprise entre 1 100 000 et 2 800 000 mètres"
                        + " pour le territoire malien.");
    }

    @Test
    void rejectsNonFiniteValues() {
        assertThatThrownBy(() -> new UtmCoordinate(Double.NaN, 1_500_000, 29, UtmCoordinate.Hemisphere.NORTH))
                .isInstanceOf(InvalidUtmCoordinateException.class);
    }
}
