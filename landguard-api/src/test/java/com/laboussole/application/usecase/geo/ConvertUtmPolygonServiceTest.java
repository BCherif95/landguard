package com.laboussole.application.usecase.geo;

import com.laboussole.domain.exception.InvalidUtmCoordinateException;
import com.laboussole.domain.model.geo.UtmCoordinate;
import com.laboussole.domain.port.in.geo.ConvertUtmPolygonUseCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConvertUtmPolygonServiceTest {

    private final ConvertUtmPolygonService service = new ConvertUtmPolygonService();

    private static UtmCoordinate vertex(double easting, double northing) {
        return new UtmCoordinate(easting, northing, 29, UtmCoordinate.Hemisphere.NORTH);
    }

    @Test
    void returnsClosedRingWithOnePointPerVertexPlusClosure() {
        var vertices = List.of(
                vertex(608_000, 1_397_000),
                vertex(608_500, 1_397_000),
                vertex(608_500, 1_397_500));

        var ring = service.execute(new ConvertUtmPolygonUseCase.Command(vertices));

        assertThat(ring).hasSize(4);
        assertThat(ring.get(0)).isEqualTo(ring.get(ring.size() - 1));
    }

    @Test
    void convertedRingStaysAroundBamakoForBamakoInput() {
        var vertices = List.of(
                vertex(608_000, 1_397_000),
                vertex(608_500, 1_397_000),
                vertex(608_500, 1_397_500));

        var ring = service.execute(new ConvertUtmPolygonUseCase.Command(vertices));

        assertThat(ring).allSatisfy(point -> {
            assertThat(point.latitude()).isBetween(12.5, 12.8);
            assertThat(point.longitude()).isBetween(-8.1, -7.9);
        });
    }

    @Test
    void rejectsFewerThanThreeVertices() {
        var vertices = List.of(vertex(608_000, 1_397_000), vertex(608_500, 1_397_000));

        assertThatThrownBy(() -> service.execute(new ConvertUtmPolygonUseCase.Command(vertices)))
                .isInstanceOf(InvalidUtmCoordinateException.class)
                .hasMessage("Un polygone requiert au moins 3 sommets.");
    }

    @Test
    void rejectsNullVertexList() {
        assertThatThrownBy(() -> service.execute(new ConvertUtmPolygonUseCase.Command(null)))
                .isInstanceOf(InvalidUtmCoordinateException.class)
                .hasMessage("Un polygone requiert au moins 3 sommets.");
    }
}
