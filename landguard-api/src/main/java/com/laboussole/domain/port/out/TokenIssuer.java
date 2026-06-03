package com.laboussole.domain.port.out;

import com.laboussole.domain.model.Role;
import com.laboussole.domain.model.UserId;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/** Driven port: signs and verifies short-lived access tokens (JWT in the JWT adapter). */
public interface TokenIssuer {

    IssuedAccessToken issueAccessToken(UserId userId, Role role);

    Optional<VerifiedAccessToken> verifyAccessToken(String compactToken);

    /** Generates an opaque, high-entropy refresh token plus its storable hash. */
    OpaqueRefreshToken issueRefreshToken();

    /** Hashes a presented refresh token for repository lookup. Same algorithm as {@link #issueRefreshToken()}. */
    String hashRefreshToken(String plaintext);

    Duration accessTokenTtl();

    Duration refreshTokenTtl();

    record IssuedAccessToken(String compactToken, Instant expiresAt) {}

    record VerifiedAccessToken(UserId userId, Role role, Instant expiresAt) {}

    record OpaqueRefreshToken(String plaintext, String hash) {}
}
