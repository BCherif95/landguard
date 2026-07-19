package com.laboussole.infrastructure.satellite;

import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.port.in.IngestSatelliteSnapshotUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Scheduled Sentinel-2 acquisition (PRD 2.1): pulls a fresh cloud-filtered
 * scene for every monitored parcel and feeds it to the regular ingestion
 * pipeline — the same pixel-difference analysis and alert routing as manual
 * uploads, no separate code path.
 *
 * <p>Runs only when Copernicus credentials are configured; parcels are
 * fault-isolated so one failed acquisition never blocks the rest.
 */
@Component
public class Sentinel2AcquisitionScheduler {

    private static final Logger log = LoggerFactory.getLogger(Sentinel2AcquisitionScheduler.class);

    private final Sentinel2Properties properties;
    private final Sentinel2ProcessClient processClient;
    private final LandParcelRepository parcels;
    private final IngestSatelliteSnapshotUseCase ingestSnapshot;
    private final Clock clock;

    public Sentinel2AcquisitionScheduler(
            Sentinel2Properties properties,
            Sentinel2ProcessClient processClient,
            LandParcelRepository parcels,
            IngestSatelliteSnapshotUseCase ingestSnapshot,
            Clock clock) {
        this.properties = properties;
        this.processClient = processClient;
        this.parcels = parcels;
        this.ingestSnapshot = ingestSnapshot;
        this.clock = clock;
    }

    @Scheduled(cron = "${laboussole.satellite.sentinel2.cron:0 0 6 * * *}", zone = "UTC")
    public void acquireAllParcels() {
        if (!properties.isConfigured()) {
            log.debug("Sentinel-2 acquisition skipped: no Copernicus credentials configured");
            return;
        }
        var monitored = parcels.findAll(properties.parcelBatchLimit());
        log.info("Sentinel-2 acquisition starting for {} parcels", monitored.size());
        int succeeded = 0;
        for (LandParcel parcel : monitored) {
            if (acquire(parcel)) {
                succeeded++;
            }
        }
        log.info("Sentinel-2 acquisition finished: {}/{} parcels ingested", succeeded, monitored.size());
    }

    private boolean acquire(LandParcel parcel) {
        try {
            Instant now = clock.instant();
            var box = BoundingBox.of(parcel.geometry(), properties.bboxPaddingRatio());
            byte[] png = processClient.fetchTrueColorPng(box, now);
            var fileName = "sentinel2-" + parcel.reference().value()
                    + "-" + LocalDate.ofInstant(now, clock.getZone()) + ".png";
            ingestSnapshot.execute(new IngestSatelliteSnapshotUseCase.Command(
                    parcel.id(), png, fileName, "image/png"));
            return true;
        } catch (Exception e) {
            log.error("Sentinel-2 acquisition failed for parcel {}: {}",
                    parcel.reference().value(), e.getMessage(), e);
            return false;
        }
    }
}
