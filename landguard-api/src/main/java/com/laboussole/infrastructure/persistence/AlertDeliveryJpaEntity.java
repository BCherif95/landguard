package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.notification.AlertChannel;
import com.laboussole.domain.model.notification.AlertDelivery;
import com.laboussole.domain.model.notification.AlertDeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alert_deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class AlertDeliveryJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "event_id", nullable = false, updatable = false, length = 36)
    private UUID eventId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "recipient_user_id", nullable = false, updatable = false, length = 36)
    private UUID recipientUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, updatable = false, length = 16)
    private AlertChannel channel;

    /**
     * Denormalised from {@link AlertChannel#dispatchPriority()} so the queue can
     * be ordered — and indexed — by urgency in SQL, without the database having
     * to know the enum's semantics.
     */
    @Column(name = "dispatch_priority", nullable = false, updatable = false)
    private int dispatchPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AlertDeliveryStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "last_error", length = AlertDelivery.MAX_ERROR_LENGTH)
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;
}
