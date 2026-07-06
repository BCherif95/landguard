package com.laboussole.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "laboussole.security.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        /*
         * Dedicated signing key for refresh tokens, so a leaked access-token key
         * cannot be used to mint long-lived refresh tokens. Falls back to {@code secret}
         * when unset; the "type" claim still prevents token-kind confusion.
         */
        String refreshSecret,
        Duration accessTokenTtl,
        Duration refreshTokenTtl) {

    public String effectiveRefreshSecret() {
        return refreshSecret == null || refreshSecret.isBlank() ? secret : refreshSecret;
    }
}
