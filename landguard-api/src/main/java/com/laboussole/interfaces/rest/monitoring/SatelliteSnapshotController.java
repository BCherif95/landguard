package com.laboussole.interfaces.rest.monitoring;

import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.in.IngestSatelliteSnapshotUseCase;
import com.laboussole.domain.port.out.SatelliteSnapshotRepository;
import com.laboussole.domain.port.out.StorageService;
import com.laboussole.interfaces.rest.monitoring.dto.SatelliteSnapshotResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/monitoring/snapshots")
@Tag(name = "Satellite Snapshots", description = "Geospatial satellite imaging data")
public class SatelliteSnapshotController {

    private final SatelliteSnapshotRepository snapshotRepository;
    private final IngestSatelliteSnapshotUseCase ingestUseCase;
    private final StorageService storage;

    public SatelliteSnapshotController(
            SatelliteSnapshotRepository snapshotRepository,
            IngestSatelliteSnapshotUseCase ingestUseCase,
            StorageService storage) {
        this.snapshotRepository = snapshotRepository;
        this.ingestUseCase = ingestUseCase;
        this.storage = storage;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    @Operation(summary = "Ingest a new satellite image for a parcel. Movement and anomaly "
            + "scores are computed server-side against the previous snapshot; an alert is "
            + "raised when the intrusion confidence exceeds 75%.")
    public SatelliteSnapshotResponse ingest(
            @RequestParam("parcelId") UUID parcelId,
            @RequestParam("file") MultipartFile file) throws IOException {
        var snapshot = ingestUseCase.execute(new IngestSatelliteSnapshotUseCase.Command(
                new ParcelId(parcelId),
                file.getBytes(),
                file.getOriginalFilename(),
                file.getContentType()));
        return SatelliteSnapshotResponse.fromDomain(snapshot);
    }

    @GetMapping("/image/{storageKey}")
    @Operation(summary = "Raw snapshot image. Public: keys are unguessable UUIDs and the "
            + "images must be displayable in map <img> tags without an Authorization header.")
    public ResponseEntity<InputStreamResource> image(@PathVariable String storageKey) {
        var resource = new InputStreamResource(storage.load(storageKey));
        var mediaType = storageKey.toLowerCase().endsWith(".png")
                ? MediaType.IMAGE_PNG
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(mediaType).body(resource);
    }

    @GetMapping
    @Operation(summary = "List satellite snapshots")
    public List<SatelliteSnapshotResponse> listSnapshots(@RequestParam(required = false) UUID parcelId) {
        if (parcelId != null) {
            return snapshotRepository.findByParcelId(new ParcelId(parcelId)).stream()
                    .map(SatelliteSnapshotResponse::fromDomain)
                    .collect(Collectors.toList());
        }
        return snapshotRepository.findAll().stream()
                .map(SatelliteSnapshotResponse::fromDomain)
                .collect(Collectors.toList());
    }
}
