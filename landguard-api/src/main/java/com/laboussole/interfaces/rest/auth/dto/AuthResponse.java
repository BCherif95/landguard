package com.laboussole.interfaces.rest.auth.dto;

import com.laboussole.domain.model.AuthTokens;
import com.laboussole.domain.model.User;

import java.time.Instant;

public record AuthResponse(
        UserResponse user,
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt) {

    public static AuthResponse of(User user, AuthTokens tokens) {
        return new AuthResponse(
                UserResponse.from(user),
                tokens.accessToken(),
                tokens.accessTokenExpiresAt(),
                tokens.refreshToken(),
                tokens.refreshTokenExpiresAt());
    }

    public static AuthResponse tokensOnly(AuthTokens tokens) {
        return new AuthResponse(
                null,
                tokens.accessToken(),
                tokens.accessTokenExpiresAt(),
                tokens.refreshToken(),
                tokens.refreshTokenExpiresAt());
    }
}
