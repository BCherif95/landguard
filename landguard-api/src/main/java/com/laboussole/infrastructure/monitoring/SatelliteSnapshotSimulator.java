package com.laboussole.infrastructure.monitoring;

import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.port.in.GenerateSatelliteSnapshotUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class SatelliteSnapshotSimulator {

    private final LandParcelRepository parcelRepository;
    private final GenerateSatelliteSnapshotUseCase generateSnapshotUseCase;
    private final Random random = new Random();

    public SatelliteSnapshotSimulator(LandParcelRepository parcelRepository, GenerateSatelliteSnapshotUseCase generateSnapshotUseCase) {
        this.parcelRepository = parcelRepository;
        this.generateSnapshotUseCase = generateSnapshotUseCase;
    }

    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void generateSnapshots() {
        log.info("Generating satellite snapshots...");
        List<LandParcel> parcels = parcelRepository.findAll(100);
        if (parcels.isEmpty()) return;

        // Generate snapshots for a few parcels
        int count = Math.min(parcels.size(), 5);
        for (int i = 0; i < count; i++) {
            LandParcel parcel = parcels.get(random.nextInt(parcels.size()));
            generateSnapshot(parcel);
        }
    }

    private void generateSnapshot(LandParcel parcel) {
        generateSnapshotUseCase.execute(new GenerateSatelliteSnapshotUseCase.Command(
                parcel.id(),
                "https://api.dicebear.com/7.x/initials/svg?seed=" + parcel.name().replace(" ", ""),
                random.nextInt(101),
                random.nextInt(101),
                "{\"cloud_cover\": " + random.nextInt(10) + ", \"resolution\": \"0.5m\"}"
        ));
        log.info("Generated satellite snapshot for parcel: {}", parcel.name());
    }
}
