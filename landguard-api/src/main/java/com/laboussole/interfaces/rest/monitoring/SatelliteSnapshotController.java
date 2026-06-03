package com.laboussole.interfaces.rest.monitoring;

import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.out.SatelliteSnapshotRepository;
import com.laboussole.interfaces.rest.monitoring.dto.SatelliteSnapshotResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/monitoring/snapshots")
@Tag(name = "Satellite Snapshots", description = "Geospatial satellite imaging data")
public class SatelliteSnapshotController {

    private final SatelliteSnapshotRepository snapshotRepository;

    public SatelliteSnapshotController(SatelliteSnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
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
