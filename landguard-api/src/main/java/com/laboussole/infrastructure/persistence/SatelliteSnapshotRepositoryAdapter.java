package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.monitoring.SatelliteSnapshot;
import com.laboussole.domain.model.monitoring.SatelliteSnapshotId;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.out.SatelliteSnapshotRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SatelliteSnapshotRepositoryAdapter implements SatelliteSnapshotRepository {

    private final SatelliteSnapshotSpringDataRepository springDataRepository;

    public SatelliteSnapshotRepositoryAdapter(SatelliteSnapshotSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public void save(SatelliteSnapshot snapshot) {
        springDataRepository.save(toEntity(snapshot));
    }

    @Override
    public Optional<SatelliteSnapshot> findById(SatelliteSnapshotId id) {
        return springDataRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<SatelliteSnapshot> findByParcelId(ParcelId parcelId) {
        return springDataRepository.findByParcelId(parcelId.value()).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<SatelliteSnapshot> findAll() {
        return springDataRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    private SatelliteSnapshotJpaEntity toEntity(SatelliteSnapshot snapshot) {
        return new SatelliteSnapshotJpaEntity(
                snapshot.id().value(),
                snapshot.parcelId().value(),
                snapshot.capturedAt(),
                snapshot.imageUrl(),
                snapshot.movementScore(),
                snapshot.anomalyScore(),
                snapshot.metadata()
        );
    }

    private SatelliteSnapshot toDomain(SatelliteSnapshotJpaEntity entity) {
        return new SatelliteSnapshot(
                new SatelliteSnapshotId(entity.getId()),
                new ParcelId(entity.getParcelId()),
                entity.getCapturedAt(),
                entity.getImageUrl(),
                entity.getMovementScore(),
                entity.getAnomalyScore(),
                entity.getMetadata()
        );
    }
}
