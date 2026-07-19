package com.laboussole.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "heirs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HeirJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SuccessionPlanJpaEntity plan;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String relation;

    @Column(name = "share_percentage", nullable = false)
    private int sharePercentage;

    @Column(nullable = false)
    private boolean validated;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "user_id", length = 36)
    private UUID userId;
}
