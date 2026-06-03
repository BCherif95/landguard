package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.monitoring.MonitoringEventId;
import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import com.laboussole.domain.model.parcel.ParcelGeometry;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.out.MonitoringRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MonitoringRepositoryAdapter implements MonitoringRepository {

    private final MonitoringEventSpringDataRepository springDataRepository;

    public MonitoringRepositoryAdapter(MonitoringEventSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public void save(MonitoringEvent event) {
        springDataRepository.save(toEntity(event));
    }

    @Override
    public Optional<MonitoringEvent> findById(MonitoringEventId id) {
        return springDataRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<MonitoringEvent> findAll() {
        return springDataRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<MonitoringEvent> findByParcelId(ParcelId parcelId) {
        return springDataRepository.findByParcelId(parcelId.value()).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<MonitoringEvent> findBySeverity(MonitoringSeverity severity) {
        return springDataRepository.findBySeverity(severity).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void delete(MonitoringEventId id) {
        springDataRepository.deleteById(id.value());
    }

    private MonitoringEventJpaEntity toEntity(MonitoringEvent event) {
        return new MonitoringEventJpaEntity(
                event.id().value(),
                event.parcelId().value(),
                event.type(),
                event.severity(),
                event.detectedAt(),
                event.confidenceScore(),
                event.coordinates().longitude(),
                event.coordinates().latitude(),
                event.description(),
                event.source(),
                event.imageUrl(),
                event.resolved()
        );
    }

    private MonitoringEvent toDomain(MonitoringEventJpaEntity entity) {
        return new MonitoringEvent(
                new MonitoringEventId(entity.getId()),
                new ParcelId(entity.getParcelId()),
                entity.getType(),
                entity.getSeverity(),
                entity.getDetectedAt(),
                entity.getConfidenceScore(),
                new ParcelGeometry.Coordinate(entity.getLongitude(), entity.getLatitude()),
                entity.getDescription(),
                entity.getSource(),
                entity.getImageUrl(),
                entity.isResolved()
        );
    }
}
