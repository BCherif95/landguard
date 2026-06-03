package com.laboussole.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "satellite_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SatelliteSnapshotJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "parcel_id", nullable = false, length = 36)
    private UUID parcelId;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "movement_score", nullable = false)
    private int movementScore;

    @Column(name = "anomaly_score", nullable = false)
    private int anomalyScore;

    @Column
    private String metadata;
}
