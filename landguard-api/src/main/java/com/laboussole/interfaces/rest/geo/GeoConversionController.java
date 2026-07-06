package com.laboussole.interfaces.rest.geo;

import com.laboussole.domain.model.geo.UtmCoordinate;
import com.laboussole.domain.port.in.geo.ConvertUtmPolygonUseCase;
import com.laboussole.interfaces.rest.geo.dto.GeoJsonPolygonResponse;
import com.laboussole.interfaces.rest.geo.dto.UtmToGeoJsonRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/geo")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Geo", description = "Generic geodesy conversions — reusable, not tied to parcel registration.")
class GeoConversionController {

    private final ConvertUtmPolygonUseCase convertUseCase;

    GeoConversionController(ConvertUtmPolygonUseCase convertUseCase) {
        this.convertUseCase = convertUseCase;
    }

    @Operation(summary = "Convert UTM vertices (zone 29N/30N) to a WGS-84 GeoJSON Polygon.")
    @PostMapping("/utm-to-geojson")
    public GeoJsonPolygonResponse utmToGeoJson(@Valid @RequestBody UtmToGeoJsonRequest request) {
        var vertices = request.points().stream()
                .map(p -> UtmCoordinate.of(p.easting(), p.northing(), request.zone()))
                .toList();
        var ring = convertUseCase.execute(new ConvertUtmPolygonUseCase.Command(vertices));
        return GeoJsonPolygonResponse.from(ring);
    }
}
