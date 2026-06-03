package com.laboussole.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Output of an authentication or refresh use case — a freshly-issued access token
 * paired with a (rotated) refresh token. The plaintext refresh value is returned
 * once to the caller and never persisted.
 */
public record AuthTokens(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        UserId userId) {

    public AuthTokens {
        Objects.requireNonNull(accessToken, "accessToken");
        Objects.requireNonNull(accessTokenExpiresAt, "accessTokenExpiresAt");
        Objects.requireNonNull(refreshToken, "refreshToken");
        Objects.requireNonNull(refreshTokenExpiresAt, "refreshTokenExpiresAt");
        Objects.requireNonNull(userId, "userId");
    }
}
