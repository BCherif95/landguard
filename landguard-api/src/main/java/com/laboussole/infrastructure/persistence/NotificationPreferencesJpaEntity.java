package com.laboussole.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
class NotificationPreferencesJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "user_id", nullable = false, updatable = false, length = 36)
    private UUID userId;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Column(name = "sms_enabled", nullable = false)
    private boolean smsEnabled;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected NotificationPreferencesJpaEntity() {}

    NotificationPreferencesJpaEntity(
            UUID userId,
            boolean pushEnabled,
            boolean emailEnabled,
            boolean smsEnabled,
            String phoneNumber,
            Instant updatedAt) {
        this.userId = userId;
        this.pushEnabled = pushEnabled;
        this.emailEnabled = emailEnabled;
        this.smsEnabled = smsEnabled;
        this.phoneNumber = phoneNumber;
        this.updatedAt = updatedAt;
    }

    UUID getUserId() { return userId; }
    boolean isPushEnabled() { return pushEnabled; }
    boolean isEmailEnabled() { return emailEnabled; }
    boolean isSmsEnabled() { return smsEnabled; }
    String getPhoneNumber() { return phoneNumber; }
    Instant getUpdatedAt() { return updatedAt; }

    void setPushEnabled(boolean pushEnabled) { this.pushEnabled = pushEnabled; }
    void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }
    void setSmsEnabled(boolean smsEnabled) { this.smsEnabled = smsEnabled; }
    void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
