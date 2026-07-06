package com.laboussole.domain.port.out;

import com.laboussole.domain.model.Role;
import com.laboussole.domain.model.UserId;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Driven port: signs and verifies self-contained tokens (JWT in the JWT adapter).
 *
 * <p>Both the access token and the refresh token are signed and carry their own
 * expiration, so no server-side token state is required.
 */
public interface TokenIssuer {

    IssuedToken issueAccessToken(UserId userId, Role role);

    Optional<VerifiedToken> verifyAccessToken(String compactToken);

    /** Issues a signed, self-expiring refresh token. Nothing is persisted server-side. */
    IssuedToken issueRefreshToken(UserId userId, Role role);

    /** Verifies signature, expiration, and that the token is a refresh token (not an access token). */
    Optional<VerifiedToken> verifyRefreshToken(String compactToken);

    Duration accessTokenTtl();

    Duration refreshTokenTtl();

    record IssuedToken(String compactToken, Instant expiresAt) {}

    record VerifiedToken(UserId userId, Role role, Instant expiresAt) {}
}
