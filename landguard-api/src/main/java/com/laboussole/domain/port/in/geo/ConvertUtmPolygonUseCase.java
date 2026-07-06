package com.laboussole.domain.port.in.geo;

import com.laboussole.domain.model.geo.UtmCoordinate;
import com.laboussole.domain.model.geo.Wgs84Point;

import java.util.List;

/**
 * Converts a polygon captured as UTM vertices (Malian situation-plan format)
 * into a closed WGS-84 ring. Generic geodesy service — deliberately not
 * coupled to parcel registration.
 */
public interface ConvertUtmPolygonUseCase {

    /** Returns the WGS-84 outer ring, closed (first point repeated last). */
    List<Wgs84Point> execute(Command command);

    record Command(List<UtmCoordinate> vertices) {}
}
