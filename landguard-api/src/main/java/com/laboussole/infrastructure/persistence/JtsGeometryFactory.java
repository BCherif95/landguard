package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.parcel.ParcelGeometry;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;

/** Bridges {@link ParcelGeometry} (domain) and JTS {@link Polygon} (Hibernate Spatial). */
final class JtsGeometryFactory {

    private static final int SRID_WGS84 = 4326;
    private static final GeometryFactory FACTORY =
            new GeometryFactory(new PrecisionModel(), SRID_WGS84);

    private JtsGeometryFactory() {}

    static Polygon toPolygon(ParcelGeometry geometry) {
        var ring = geometry.outerRing();
        var coords = new Coordinate[ring.size()];
        for (int i = 0; i < ring.size(); i++) {
            var p = ring.get(i);
            coords[i] = new Coordinate(p.longitude(), p.latitude());
        }
        var polygon = FACTORY.createPolygon(coords);
        polygon.setSRID(SRID_WGS84);
        return polygon;
    }

    static ParcelGeometry toDomain(Polygon polygon) {
        var coords = polygon.getExteriorRing().getCoordinates();
        var points = new java.util.ArrayList<ParcelGeometry.Coordinate>(coords.length);
        for (var c : coords) {
            points.add(new ParcelGeometry.Coordinate(c.x, c.y));
        }
        return new ParcelGeometry(List.copyOf(points));
    }
}
