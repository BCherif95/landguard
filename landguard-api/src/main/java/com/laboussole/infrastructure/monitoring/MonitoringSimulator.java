package com.laboussole.infrastructure.monitoring;

import com.laboussole.domain.model.monitoring.MonitoringEventType;
import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import com.laboussole.domain.model.monitoring.MonitoringSource;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.ParcelGeometry;
import com.laboussole.domain.port.in.DetectMonitoringEventUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class MonitoringSimulator {

    private final LandParcelRepository parcelRepository;
    private final DetectMonitoringEventUseCase detectEventUseCase;
    private final Random random = new Random();

    public MonitoringSimulator(LandParcelRepository parcelRepository, DetectMonitoringEventUseCase detectEventUseCase) {
        this.parcelRepository = parcelRepository;
        this.detectEventUseCase = detectEventUseCase;
    }

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    @Transactional
    public void simulateActivity() {
        log.info("Simulating monitoring activity...");
        List<LandParcel> parcels = parcelRepository.findAll(100);
        if (parcels.isEmpty()) return;

        // Pick 1-3 random parcels to generate events for
        int count = random.nextInt(3) + 1;
        for (int i = 0; i < count; i++) {
            LandParcel parcel = parcels.get(random.nextInt(parcels.size()));
            generateEvent(parcel);
        }
    }

    private void generateEvent(LandParcel parcel) {
        MonitoringEventType type = MonitoringEventType.values()[random.nextInt(MonitoringEventType.values().length)];
        if (type == MonitoringEventType.UNKNOWN) type = MonitoringEventType.CONSTRUCTION;

        MonitoringSeverity severity = MonitoringSeverity.values()[random.nextInt(MonitoringSeverity.values().length)];
        MonitoringSource source = MonitoringSource.values()[random.nextInt(MonitoringSource.values().length)];

        // Get center or a random point within the parcel geometry if possible
        // For simplicity, we use the first point of the outer ring
        ParcelGeometry.Coordinate coord = parcel.geometry().outerRing().get(0);

        String description = String.format("Detected %s activity on parcel %s via %s",
                type.name().toLowerCase(), parcel.name(), source.name().toLowerCase());

        detectEventUseCase.execute(new DetectMonitoringEventUseCase.Command(
                parcel.id(),
                type,
                severity,
                random.nextInt(40) + 60, // 60-100 confidence
                coord,
                description,
                source,
                "https://api.dicebear.com/7.x/identicon/svg?seed=" + parcel.id().toString()
        ));
        log.info("Generated simulation event for parcel: {}", parcel.name());
    }
}
