package com.laboussole.application.usecase.geo;

import com.laboussole.domain.exception.InvalidUtmCoordinateException;
import com.laboussole.domain.model.geo.UtmToWgs84Converter;
import com.laboussole.domain.model.geo.Wgs84Point;
import com.laboussole.domain.port.in.geo.ConvertUtmPolygonUseCase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConvertUtmPolygonService implements ConvertUtmPolygonUseCase {

    private static final int MIN_VERTICES = 3;

    @Override
    public List<Wgs84Point> execute(Command command) {
        var vertices = command.vertices();
        if (vertices == null || vertices.size() < MIN_VERTICES) {
            throw new InvalidUtmCoordinateException(
                    "Un polygone requiert au moins 3 sommets.");
        }
        List<Wgs84Point> ring = new ArrayList<>(vertices.size() + 1);
        for (var vertex : vertices) {
            ring.add(UtmToWgs84Converter.convert(vertex));
        }
        ring.add(ring.get(0));
        return List.copyOf(ring);
    }
}
