package com.laboussole.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "succession_votes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuccessionVoteJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "succession_plan_id", nullable = false, length = 36)
    private UUID successionPlanId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "heir_id", nullable = false, length = 36)
    private UUID heirId;

    @Column(nullable = false)
    private boolean approved;

    @Column(name = "voted_at", nullable = false)
    private Instant votedAt;

    @Column(name = "ip_address")
    private String ipAddress;
}
