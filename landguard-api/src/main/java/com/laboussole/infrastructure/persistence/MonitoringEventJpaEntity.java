package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.monitoring.MonitoringEventType;
import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import com.laboussole.domain.model.monitoring.MonitoringSource;
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
@Table(name = "monitoring_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringEventJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "parcel_id", nullable = false, length = 36)
    private UUID parcelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonitoringEventType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonitoringSeverity severity;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(name = "confidence_score", nullable = false)
    private int confidenceScore;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonitoringSource source;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private boolean resolved;
}
