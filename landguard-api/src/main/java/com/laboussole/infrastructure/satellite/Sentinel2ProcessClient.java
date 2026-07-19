package com.laboussole.infrastructure.satellite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Sentinel Hub Process API client (Copernicus Data Space): requests a
 * true-color composite of the most recent cloud-filtered Sentinel-2 L2A
 * scenes over a bounding box, returned as PNG bytes.
 */
@Component
class Sentinel2ProcessClient {

    /**
     * True-color rendering from the 10 m visible bands. The 2.5 gain is the
     * standard brightening factor for Sentinel-2 reflectance values.
     */
    static final String TRUE_COLOR_EVALSCRIPT = """
            //VERSION=3
            function setup() {
              return { input: ["B02", "B03", "B04"], output: { bands: 3 } };
            }
            function evaluatePixel(sample) {
              return [2.5 * sample.B04, 2.5 * sample.B03, 2.5 * sample.B02];
            }
            """;

    private final Sentinel2Properties properties;
    private final CopernicusAuthClient authClient;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    Sentinel2ProcessClient(
            Sentinel2Properties properties,
            CopernicusAuthClient authClient,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.authClient = authClient;
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /** Fetches the latest true-color PNG over the box; empty scenes fail with an API error. */
    byte[] fetchTrueColorPng(BoundingBox box, Instant now) {
        byte[] image = restClient.post()
                .uri(properties.processUrl())
                .header("Authorization", "Bearer " + authClient.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.IMAGE_PNG)
                .body(buildRequestBody(box, now).toString())
                .retrieve()
                .body(byte[].class);
        if (image == null || image.length == 0) {
            throw new IllegalStateException("Sentinel-2 Process API returned an empty image");
        }
        return image;
    }

    ObjectNode buildRequestBody(BoundingBox box, Instant now) {
        var root = objectMapper.createObjectNode();

        var bounds = root.putObject("input").putObject("bounds");
        bounds.putArray("bbox")
                .add(box.west()).add(box.south()).add(box.east()).add(box.north());
        bounds.putObject("properties")
                .put("crs", "http://www.opengis.net/def/crs/EPSG/0/4326");

        var data = ((ObjectNode) root.get("input")).putArray("data").addObject();
        data.put("type", "sentinel-2-l2a");
        var dataFilter = data.putObject("dataFilter");
        var timeRange = dataFilter.putObject("timeRange");
        timeRange.put("from", now.minus(properties.lookbackDays(), ChronoUnit.DAYS).toString());
        timeRange.put("to", now.toString());
        dataFilter.put("maxCloudCoverage", properties.maxCloudCoverage());
        dataFilter.put("mosaickingOrder", "mostRecent");

        var output = root.putObject("output");
        output.put("width", properties.imageWidthPx());
        output.put("height", box.heightFor(properties.imageWidthPx()));
        output.putArray("responses").addObject()
                .put("identifier", "default")
                .putObject("format").put("type", "image/png");

        root.put("evalscript", TRUE_COLOR_EVALSCRIPT);
        return root;
    }
}
