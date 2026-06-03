package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.heritage.SuccessionAuditType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "succession_audit_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuccessionAuditEventJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "succession_plan_id", nullable = false, length = 36)
    private UUID successionPlanId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuccessionAuditType type;

    @Column(nullable = false)
    private String actor;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private Map<String, String> metadata;
}
