package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.Role;
import com.laboussole.domain.model.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
class UserJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @Column(name = "email", nullable = false, length = 254, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private UserStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    protected UserJpaEntity() {}

    UserJpaEntity(
            UUID id,
            String email,
            String fullName,
            String passwordHash,
            Role role,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt,
            Instant lastLoginAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    UUID getId() { return id; }
    String getEmail() { return email; }
    String getFullName() { return fullName; }
    String getPasswordHash() { return passwordHash; }
    Role getRole() { return role; }
    UserStatus getStatus() { return status; }
    Instant getCreatedAt() { return createdAt; }
    Instant getUpdatedAt() { return updatedAt; }
    Instant getLastLoginAt() { return lastLoginAt; }

    void setEmail(String email) { this.email = email; }
    void setFullName(String fullName) { this.fullName = fullName; }
    void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    void setRole(Role role) { this.role = role; }
    void setStatus(UserStatus status) { this.status = status; }
    void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
