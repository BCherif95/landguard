package com.laboussole.infrastructure.security;

import com.laboussole.domain.model.Role;
import com.laboussole.domain.model.UserId;
import com.laboussole.domain.port.out.TokenIssuer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
class JwtTokenIssuer implements TokenIssuer {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey accessSigningKey;
    private final SecretKey refreshSigningKey;

    JwtTokenIssuer(JwtProperties properties) {
        this.properties = properties;
        this.accessSigningKey = Keys.hmacShaKeyFor(decodeSecret(properties.secret()));
        this.refreshSigningKey = Keys.hmacShaKeyFor(decodeSecret(properties.effectiveRefreshSecret()));
    }

    @Override
    public IssuedToken issueAccessToken(UserId userId, Role role) {
        return issue(userId, role, TYPE_ACCESS, properties.accessTokenTtl(), accessSigningKey);
    }

    @Override
    public Optional<VerifiedToken> verifyAccessToken(String compactToken) {
        return verify(compactToken, TYPE_ACCESS, accessSigningKey);
    }

    @Override
    public IssuedToken issueRefreshToken(UserId userId, Role role) {
        return issue(userId, role, TYPE_REFRESH, properties.refreshTokenTtl(), refreshSigningKey);
    }

    @Override
    public Optional<VerifiedToken> verifyRefreshToken(String compactToken) {
        return verify(compactToken, TYPE_REFRESH, refreshSigningKey);
    }

    @Override
    public Duration accessTokenTtl() {
        return properties.accessTokenTtl();
    }

    @Override
    public Duration refreshTokenTtl() {
        return properties.refreshTokenTtl();
    }

    private IssuedToken issue(UserId userId, Role role, String type, Duration ttl, SecretKey key) {
        var now = Instant.now();
        var expiresAt = now.plus(ttl);
        var compact = Jwts.builder()
                .issuer(properties.issuer())
                .subject(userId.asString())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_TYPE, type)
                .signWith(key)
                .compact();
        return new IssuedToken(compact, expiresAt);
    }

    private Optional<VerifiedToken> verify(String compactToken, String expectedType, SecretKey key) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(properties.issuer())
                    .build()
                    .parseSignedClaims(compactToken)
                    .getPayload();
            // The type claim prevents an access token from being replayed as a refresh
            // token (and vice versa) when both kinds share the same signing key.
            var type = claims.get(CLAIM_TYPE, String.class);
            if (!expectedType.equals(type)) {
                return Optional.empty();
            }
            var userId = UserId.of(UUID.fromString(claims.getSubject()));
            var role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));
            return Optional.of(new VerifiedToken(userId, role, claims.getExpiration().toInstant()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static byte[] decodeSecret(String secret) {
        // Accept either base64-encoded or raw secrets, as long as the resulting key is >= 32 bytes for HS256.
        try {
            var decoded = Base64.getDecoder().decode(secret);
            if (decoded.length >= 32) return decoded;
        } catch (IllegalArgumentException ignored) {
            // fall through to raw
        }
        var raw = secret.getBytes(StandardCharsets.UTF_8);
        if (raw.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes (set laboussole.security.jwt.secret).");
        }
        return raw;
    }
}
