package com.laboussole.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Refresh token entity, scoped to a {@link User}.
 *
 * <p>Refresh tokens are stored as a hash (never plaintext) and rotate on every use:
 * each successful refresh revokes the parent and creates a child token chained via
 * {@link #replacedBy()}. This allows reuse-detection: if a revoked token is presented
 * again, the entire chain can be invalidated.
 */
public final class RefreshToken {

    private final RefreshTokenId id;
    private final UserId userId;
    private final String tokenHash;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private Instant revokedAt;
    private RefreshTokenId replacedBy;

    private RefreshToken(
            RefreshTokenId id,
            UserId userId,
            String tokenHash,
            Instant issuedAt,
            Instant expiresAt,
            Instant revokedAt,
            RefreshTokenId replacedBy) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.tokenHash = requireNonBlank(tokenHash, "tokenHash");
        this.issuedAt = Objects.requireNonNull(issuedAt);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.revokedAt = revokedAt;
        this.replacedBy = replacedBy;
    }

    public static RefreshToken issue(UserId userId, String tokenHash, Duration ttl) {
        var now = Instant.now();
        return new RefreshToken(
                RefreshTokenId.generate(),
                userId,
                tokenHash,
                now,
                now.plus(ttl),
                null,
                null);
    }

    public static RefreshToken reconstitute(
            RefreshTokenId id,
            UserId userId,
            String tokenHash,
            Instant issuedAt,
            Instant expiresAt,
            Instant revokedAt,
            RefreshTokenId replacedBy) {
        return new RefreshToken(id, userId, tokenHash, issuedAt, expiresAt, revokedAt, replacedBy);
    }

    public boolean isActive() {
        return revokedAt == null && Instant.now().isBefore(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void revoke(RefreshTokenId replacement) {
        if (this.revokedAt == null) {
            this.revokedAt = Instant.now();
        }
        this.replacedBy = replacement;
    }

    public void revoke() {
        revoke(null);
    }

    public RefreshTokenId id() { return id; }
    public UserId userId() { return userId; }
    public String tokenHash() { return tokenHash; }
    public Instant issuedAt() { return issuedAt; }
    public Instant expiresAt() { return expiresAt; }
    public Optional<Instant> revokedAt() { return Optional.ofNullable(revokedAt); }
    public Optional<RefreshTokenId> replacedBy() { return Optional.ofNullable(replacedBy); }

    private static String requireNonBlank(String v, String field) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return v;
    }
}
