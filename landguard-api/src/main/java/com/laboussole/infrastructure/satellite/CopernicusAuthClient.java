package com.laboussole.infrastructure.satellite;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;

/**
 * OAuth2 client-credentials flow against the Copernicus Data Space identity
 * server. Tokens are cached until shortly before expiry.
 */
@Component
class CopernicusAuthClient {

    /** Refresh margin so a token never expires mid-request. */
    private static final long EXPIRY_MARGIN_SECONDS = 60;

    private final Sentinel2Properties properties;
    private final RestClient restClient;

    private volatile String cachedToken;
    private volatile Instant cachedTokenExpiry = Instant.EPOCH;

    CopernicusAuthClient(Sentinel2Properties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    synchronized String accessToken() {
        if (cachedToken != null && Instant.now().isBefore(cachedTokenExpiry)) {
            return cachedToken;
        }
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());

        JsonNode response = restClient.post()
                .uri(properties.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JsonNode.class);
        if (response == null || !response.hasNonNull("access_token")) {
            throw new IllegalStateException("Copernicus token endpoint returned no access_token");
        }
        cachedToken = response.get("access_token").asText();
        long expiresIn = response.path("expires_in").asLong(600);
        cachedTokenExpiry = Instant.now().plusSeconds(Math.max(0, expiresIn - EXPIRY_MARGIN_SECONDS));
        return cachedToken;
    }
}
