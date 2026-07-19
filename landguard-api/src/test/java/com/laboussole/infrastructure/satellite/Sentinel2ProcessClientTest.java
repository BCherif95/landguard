package com.laboussole.infrastructure.satellite;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * The Process API request must pin every PRD constraint: L2A source, cloud
 * filter, most-recent mosaicking, WGS84 bbox and a true-color PNG output.
 */
class Sentinel2ProcessClientTest {

    private final Sentinel2Properties properties = new Sentinel2Properties(
            "client", "secret", "https://token.example", "https://process.example",
            10, 20, 512, 0.2, 500);

    private final Sentinel2ProcessClient client = new Sentinel2ProcessClient(
            properties, mock(CopernicusAuthClient.class), RestClient.builder(), new ObjectMapper());

    @Test
    void requestPinsSourceCloudFilterAndTimeRange() {
        var now = Instant.parse("2026-07-19T06:00:00Z");
        var body = client.buildRequestBody(new BoundingBox(-8.0, 12.6, -7.9, 12.7), now);

        var data = body.path("input").path("data").get(0);
        assertEquals("sentinel-2-l2a", data.path("type").asText());
        assertEquals(20, data.path("dataFilter").path("maxCloudCoverage").asInt());
        assertEquals("mostRecent", data.path("dataFilter").path("mosaickingOrder").asText());
        assertEquals("2026-07-09T06:00:00Z", data.path("dataFilter").path("timeRange").path("from").asText());
        assertEquals("2026-07-19T06:00:00Z", data.path("dataFilter").path("timeRange").path("to").asText());
    }

    @Test
    void requestUsesWgs84BboxAndAspectPreservingOutput() {
        var body = client.buildRequestBody(new BoundingBox(-8.0, 12.6, -7.9, 12.7), Instant.now());

        var bounds = body.path("input").path("bounds");
        assertEquals(-8.0, bounds.path("bbox").get(0).asDouble(), 1e-9);
        assertEquals(12.7, bounds.path("bbox").get(3).asDouble(), 1e-9);
        assertTrue(bounds.path("properties").path("crs").asText().endsWith("EPSG/0/4326"));

        assertEquals(512, body.path("output").path("width").asInt());
        assertEquals(512, body.path("output").path("height").asInt());
        assertEquals("image/png",
                body.path("output").path("responses").get(0).path("format").path("type").asText());
    }

    @Test
    void evalscriptRendersTrueColorFromVisibleBands() {
        var body = client.buildRequestBody(new BoundingBox(-8.0, 12.6, -7.9, 12.7), Instant.now());

        var evalscript = body.path("evalscript").asText();
        assertTrue(evalscript.contains("B02"));
        assertTrue(evalscript.contains("B03"));
        assertTrue(evalscript.contains("B04"));
    }
}
