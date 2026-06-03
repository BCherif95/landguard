package com.laboussole.application.usecase.monitoring;

import com.laboussole.domain.model.monitoring.SatelliteSnapshot;
import com.laboussole.domain.port.in.GenerateSatelliteSnapshotUseCase;
import com.laboussole.domain.port.out.SatelliteSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenerateSatelliteSnapshotService implements GenerateSatelliteSnapshotUseCase {

    private final SatelliteSnapshotRepository repository;

    public GenerateSatelliteSnapshotService(SatelliteSnapshotRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public SatelliteSnapshot execute(Command command) {
        var snapshot = SatelliteSnapshot.create(
                command.parcelId(),
                command.imageUrl(),
                command.movementScore(),
                command.anomalyScore(),
                command.metadata()
        );
        repository.save(snapshot);
        return snapshot;
    }
}
