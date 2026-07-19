package com.laboussole.infrastructure.satellite;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Copernicus Data Space (Sentinel-2) acquisition settings — PRD 2.1: standard
 * imagery feed, 10 m resolution, ~5-day revisit over Mali.
 *
 * <p>Credentials are always injected from the environment. Blank credentials
 * are tolerated at boot so local dev works without a Copernicus account; the
 * scheduled acquisition then skips with a log instead of running.
 */
@ConfigurationProperties(prefix = "laboussole.satellite.sentinel2")
public record Sentinel2Properties(
        String clientId,
        String clientSecret,
        String tokenUrl,
        String processUrl,
        int lookbackDays,
        int maxCloudCoverage,
        int imageWidthPx,
        double bboxPaddingRatio,
        int parcelBatchLimit) {

    public boolean isConfigured() {
        return isNotBlank(clientId) && isNotBlank(clientSecret);
    }

    private static boolean isNotBlank(String v) {
        return v != null && !v.isBlank();
    }
}
