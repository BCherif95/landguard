package com.laboussole.domain.port.in;

import com.laboussole.domain.model.AuthTokens;

/** Driving port: rotate a refresh token and mint a new access token. */
public interface RefreshAccessTokenUseCase {

    AuthTokens execute(Command command);

    record Command(String refreshToken) {}
}
