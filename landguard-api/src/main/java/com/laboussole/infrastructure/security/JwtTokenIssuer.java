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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Component
class JwtTokenIssuer implements TokenIssuer {

    private static final String CLAIM_ROLE = "role";

    private final JwtProperties properties;
    private final SecretKey signingKey;
    private final SecureRandom random = new SecureRandom();

    JwtTokenIssuer(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(decodeSecret(properties.secret()));
    }

    @Override
    public IssuedAccessToken issueAccessToken(UserId userId, Role role) {
        var now = Instant.now();
        var expiresAt = now.plus(properties.accessTokenTtl());
        var compact = Jwts.builder()
                .issuer(properties.issuer())
                .subject(userId.asString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim(CLAIM_ROLE, role.name())
                .signWith(signingKey)
                .compact();
        return new IssuedAccessToken(compact, expiresAt);
    }

    @Override
    public Optional<VerifiedAccessToken> verifyAccessToken(String compactToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(properties.issuer())
                    .build()
                    .parseSignedClaims(compactToken)
                    .getPayload();
            var userId = UserId.of(UUID.fromString(claims.getSubject()));
            var role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));
            return Optional.of(new VerifiedAccessToken(userId, role, claims.getExpiration().toInstant()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public OpaqueRefreshToken issueRefreshToken() {
        var bytes = new byte[48];
        random.nextBytes(bytes);
        var plaintext = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new OpaqueRefreshToken(plaintext, hashRefreshToken(plaintext));
    }

    @Override
    public String hashRefreshToken(String plaintext) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(plaintext.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    @Override
    public Duration accessTokenTtl() {
        return properties.accessTokenTtl();
    }

    @Override
    public Duration refreshTokenTtl() {
        return properties.refreshTokenTtl();
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
