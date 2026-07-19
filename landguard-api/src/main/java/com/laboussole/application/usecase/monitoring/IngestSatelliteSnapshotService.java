package com.laboussole.application.usecase.monitoring;

import com.laboussole.application.service.PixelDifferenceAnalyzer;
import com.laboussole.domain.model.monitoring.MonitoringEventType;
import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import com.laboussole.domain.model.monitoring.MonitoringSource;
import com.laboussole.domain.model.monitoring.SatelliteSnapshot;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.ParcelGeometry;
import com.laboussole.domain.port.in.DetectMonitoringEventUseCase;
import com.laboussole.domain.port.in.IngestSatelliteSnapshotUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.SatelliteSnapshotRepository;
import com.laboussole.domain.port.out.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

/**
 * Real imagery pipeline (PRD Features 03.1 / 4.1): stores the uploaded image,
 * compares it against the parcel's previous snapshot with the pixel-difference
 * engine, persists the scored snapshot, and raises a monitoring event (which
 * fans out to the multi-channel alert routing) when the structural-intrusion
 * confidence exceeds the PRD's 75% threshold.
 */
@Service
public class IngestSatelliteSnapshotService implements IngestSatelliteSnapshotUseCase {

    /** PRD 4.1: an alert is raised above 75% intrusion confidence. */
    static final int ALERT_CONFIDENCE_THRESHOLD = 75;
    /** Public (unguessable-key) image endpoint the stored URL points to. */
    static final String IMAGE_URL_PREFIX = "/api/v1/monitoring/snapshots/image/";

    private static final Logger log = LoggerFactory.getLogger(IngestSatelliteSnapshotService.class);

    private final LandParcelRepository parcels;
    private final SatelliteSnapshotRepository snapshots;
    private final StorageService storage;
    private final PixelDifferenceAnalyzer analyzer;
    private final DetectMonitoringEventUseCase detectEvent;

    public IngestSatelliteSnapshotService(
            LandParcelRepository parcels,
            SatelliteSnapshotRepository snapshots,
            StorageService storage,
            PixelDifferenceAnalyzer analyzer,
            DetectMonitoringEventUseCase detectEvent) {
        this.parcels = parcels;
        this.snapshots = snapshots;
        this.storage = storage;
        this.analyzer = analyzer;
        this.detectEvent = detectEvent;
    }

    @Override
    @Transactional
    public SatelliteSnapshot execute(Command command) {
        LandParcel parcel = parcels.findById(command.parcelId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Parcel not found: " + command.parcelId().value()));

        BufferedImage image = decode(command.imageContent());
        var previous = latestSnapshot(parcel);

        var analysis = previous
                .flatMap(this::loadSnapshotImage)
                .map(previousImage -> analyzer.analyze(previousImage, image));

        String storageKey = storage.store(
                new ByteArrayInputStream(command.imageContent()),
                command.fileName(),
                command.contentType());

        int movementScore = analysis.map(PixelDifferenceAnalyzer.ImageryAnalysis::structuralChangeScore).orElse(0);
        int anomalyScore = analysis.map(PixelDifferenceAnalyzer.ImageryAnalysis::confidenceScore).orElse(0);
        boolean seasonal = analysis.map(PixelDifferenceAnalyzer.ImageryAnalysis::seasonalShiftDetected).orElse(false);

        var snapshot = SatelliteSnapshot.create(
                command.parcelId(),
                IMAGE_URL_PREFIX + storageKey,
                movementScore,
                anomalyScore,
                metadataJson(storageKey, analysis.isPresent(), seasonal));
        snapshots.save(snapshot);

        if (anomalyScore >= ALERT_CONFIDENCE_THRESHOLD && !seasonal) {
            raiseIntrusionEvent(parcel, snapshot, movementScore, anomalyScore);
        }
        return snapshot;
    }

    private void raiseIntrusionEvent(
            LandParcel parcel, SatelliteSnapshot snapshot, int movementScore, int confidence) {
        var severity = movementScore >= 60 ? MonitoringSeverity.CRITICAL : MonitoringSeverity.HIGH;
        detectEvent.execute(new DetectMonitoringEventUseCase.Command(
                parcel.id(),
                MonitoringEventType.CONSTRUCTION,
                severity,
                confidence,
                centroid(parcel.geometry()),
                String.format(Locale.FRANCE,
                        "Changement structurel détecté sur la parcelle %s "
                                + "(indice de mouvement %d%%, confiance %d%%).",
                        parcel.name(), movementScore, confidence),
                MonitoringSource.AI_DETECTION,
                snapshot.imageUrl()));
    }

    private Optional<SatelliteSnapshot> latestSnapshot(LandParcel parcel) {
        return snapshots.findByParcelId(parcel.id()).stream()
                .max(Comparator.comparing(SatelliteSnapshot::capturedAt));
    }

    /**
     * Previous snapshots reference their image through the public URL; the
     * storage key is its last path segment. A snapshot whose image is no longer
     * readable simply yields no comparison (first-baseline semantics).
     */
    private Optional<BufferedImage> loadSnapshotImage(SatelliteSnapshot snapshot) {
        String url = snapshot.imageUrl();
        String key = url.substring(url.lastIndexOf('/') + 1);
        try (InputStream in = storage.load(key)) {
            return Optional.ofNullable(ImageIO.read(in));
        } catch (Exception e) {
            log.warn("Previous snapshot image {} unreadable, treating upload as new baseline: {}",
                    key, e.getMessage());
            return Optional.empty();
        }
    }

    private static BufferedImage decode(byte[] content) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
            if (image == null) {
                throw new IllegalArgumentException("Unsupported or corrupt image format.");
            }
            return image;
        } catch (IOException e) {
            throw new UncheckedIOException("Unreadable image payload", e);
        }
    }

    private static ParcelGeometry.Coordinate centroid(ParcelGeometry geometry) {
        var ring = geometry.outerRing();
        // The ring is closed (first == last): skip the duplicate closing point.
        int points = ring.size() - 1;
        double lon = 0, lat = 0;
        for (int i = 0; i < points; i++) {
            lon += ring.get(i).longitude();
            lat += ring.get(i).latitude();
        }
        return new ParcelGeometry.Coordinate(lon / points, lat / points);
    }

    private static String metadataJson(String storageKey, boolean compared, boolean seasonal) {
        return String.format(
                "{\"engine\":\"pixel-diff-v1\",\"storageKey\":\"%s\",\"comparedToPrevious\":%b,"
                        + "\"seasonalShiftDetected\":%b,\"capturedAt\":\"%s\"}",
                storageKey, compared, seasonal, Instant.now());
    }
}
