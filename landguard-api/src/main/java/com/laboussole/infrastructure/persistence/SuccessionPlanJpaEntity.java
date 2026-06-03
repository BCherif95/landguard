package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.heritage.SuccessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "succession_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuccessionPlanJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "parcel_id", nullable = false, length = 36)
    private UUID parcelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuccessionStatus status;

    @Column(name = "blockchain_hash")
    private String blockchainHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<HeirJpaEntity> heirs;
}
