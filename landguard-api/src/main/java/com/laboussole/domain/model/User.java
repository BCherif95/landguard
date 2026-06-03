package com.laboussole.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root for an authenticated principal of LA BOUSSOLE.
 * Encapsulates identity, credentials, RBAC role, and lifecycle state.
 *
 * <p>Plaintext passwords never enter the domain — credentials are always
 * supplied as a {@link HashedPassword} produced by the {@code PasswordHasher} port.
 */
public final class User {

    private final UserId id;
    private final Email email;
    private final String fullName;
    private HashedPassword passwordHash;
    private Role role;
    private UserStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;

    private User(
            UserId id,
            Email email,
            String fullName,
            HashedPassword passwordHash,
            Role role,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt,
            Instant lastLoginAt) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.fullName = requireFullName(fullName);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.role = Objects.requireNonNull(role);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.lastLoginAt = lastLoginAt;
    }

    /** Factory for a brand-new account in {@link UserStatus#ACTIVE} state. */
    public static User register(Email email, String fullName, HashedPassword passwordHash, Role role) {
        var now = Instant.now();
        return new User(
                UserId.generate(),
                email,
                fullName,
                passwordHash,
                role,
                UserStatus.ACTIVE,
                now,
                now,
                null);
    }

    /** Reconstitution from persistence — does not run lifecycle invariants. */
    public static User reconstitute(
            UserId id,
            Email email,
            String fullName,
            HashedPassword passwordHash,
            Role role,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt,
            Instant lastLoginAt) {
        return new User(id, email, fullName, passwordHash, role, status, createdAt, updatedAt, lastLoginAt);
    }

    public void recordSuccessfulLogin() {
        this.lastLoginAt = Instant.now();
        this.updatedAt = this.lastLoginAt;
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
        this.updatedAt = Instant.now();
    }

    public void changeRole(Role newRole) {
        this.role = Objects.requireNonNull(newRole);
        this.updatedAt = Instant.now();
    }

    public void changePassword(HashedPassword newHash) {
        this.passwordHash = Objects.requireNonNull(newHash);
        this.updatedAt = Instant.now();
    }

    public boolean canAuthenticate() {
        return status.canAuthenticate();
    }

    public UserId id() { return id; }
    public Email email() { return email; }
    public String fullName() { return fullName; }
    public HashedPassword passwordHash() { return passwordHash; }
    public Role role() { return role; }
    public UserStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Instant lastLoginAt() { return lastLoginAt; }

    private static String requireFullName(String v) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name must not be blank");
        }
        if (v.length() > 200) {
            throw new IllegalArgumentException("Full name must not exceed 200 characters");
        }
        return v.trim();
    }
}
