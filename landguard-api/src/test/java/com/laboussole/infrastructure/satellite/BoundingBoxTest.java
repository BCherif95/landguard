package com.laboussole.infrastructure.satellite;

import com.laboussole.domain.model.parcel.ParcelGeometry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoundingBoxTest {

    private final ParcelGeometry geometry = new ParcelGeometry(List.of(
            new ParcelGeometry.Coordinate(-8.00, 12.63),
            new ParcelGeometry.Coordinate(-8.00, 12.64),
            new ParcelGeometry.Coordinate(-7.98, 12.64),
            new ParcelGeometry.Coordinate(-8.00, 12.63)));

    @Test
    void wrapsTheOuterRing() {
        var box = BoundingBox.of(geometry, 0.0);

        assertEquals(-8.00, box.west(), 1e-9);
        assertEquals(12.63, box.south(), 1e-9);
        assertEquals(-7.98, box.east(), 1e-9);
        assertEquals(12.64, box.north(), 1e-9);
    }

    @Test
    void paddingKeepsSurroundingTerrainVisible() {
        var box = BoundingBox.of(geometry, 0.5);

        assertEquals(-8.01, box.west(), 1e-9);
        assertEquals(12.625, box.south(), 1e-9);
        assertEquals(-7.97, box.east(), 1e-9);
        assertEquals(12.645, box.north(), 1e-9);
    }

    @Test
    void imageHeightPreservesTheAspectRatio() {
        var box = new BoundingBox(-8.00, 12.63, -7.98, 12.64);

        // bbox is twice as wide as tall -> half the width in pixels.
        assertEquals(256, box.heightFor(512));
    }
}
