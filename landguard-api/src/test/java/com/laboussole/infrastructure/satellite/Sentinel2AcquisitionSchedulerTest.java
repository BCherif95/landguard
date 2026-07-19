package com.laboussole.infrastructure.satellite;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.parcel.CadastralReference;
import com.laboussole.domain.model.parcel.Hectares;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.MoneyXof;
import com.laboussole.domain.model.parcel.ParcelGeometry;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.model.parcel.ParcelStatus;
import com.laboussole.domain.port.in.IngestSatelliteSnapshotUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Sentinel2AcquisitionSchedulerTest {

    @Mock private Sentinel2ProcessClient processClient;
    @Mock private LandParcelRepository parcelRepository;
    @Mock private IngestSatelliteSnapshotUseCase ingestSnapshot;

    private final Clock clock =
            Clock.fixed(Instant.parse("2026-07-19T06:00:00Z"), ZoneOffset.UTC);

    @Test
    void withoutCredentialsTheAcquisitionIsSkippedEntirely() {
        var scheduler = new Sentinel2AcquisitionScheduler(
                properties("", ""), processClient, parcelRepository, ingestSnapshot, clock);

        scheduler.acquireAllParcels();

        verifyNoInteractions(parcelRepository, processClient, ingestSnapshot);
    }

    @Test
    void acquiredSceneFeedsTheRegularIngestionPipeline() {
        var parcel = parcel("BSL-ML-2024-000123");
        when(parcelRepository.findAll(500)).thenReturn(List.of(parcel));
        byte[] png = {1, 2, 3};
        when(processClient.fetchTrueColorPng(any(), any())).thenReturn(png);

        scheduler().acquireAllParcels();

        var command = ArgumentCaptor.forClass(IngestSatelliteSnapshotUseCase.Command.class);
        verify(ingestSnapshot).execute(command.capture());
        assertEquals(parcel.id(), command.getValue().parcelId());
        assertArrayEquals(png, command.getValue().imageContent());
        assertEquals("sentinel2-BSL-ML-2024-000123-2026-07-19.png", command.getValue().fileName());
        assertEquals("image/png", command.getValue().contentType());
    }

    @Test
    void oneFailedParcelDoesNotBlockTheOthers() {
        var failing = parcel("BSL-ML-2024-000001");
        var healthy = parcel("BSL-ML-2024-000002");
        when(parcelRepository.findAll(500)).thenReturn(List.of(failing, healthy));
        when(processClient.fetchTrueColorPng(any(), any()))
                .thenThrow(new IllegalStateException("Copernicus quota exceeded"))
                .thenReturn(new byte[]{1});

        scheduler().acquireAllParcels();

        var command = ArgumentCaptor.forClass(IngestSatelliteSnapshotUseCase.Command.class);
        verify(ingestSnapshot).execute(command.capture());
        assertEquals(healthy.id(), command.getValue().parcelId());
        verify(ingestSnapshot, never()).execute(
                org.mockito.ArgumentMatchers.argThat(c -> c.parcelId().equals(failing.id())));
    }

    private Sentinel2AcquisitionScheduler scheduler() {
        return new Sentinel2AcquisitionScheduler(
                properties("client", "secret"), processClient, parcelRepository, ingestSnapshot, clock);
    }

    private Sentinel2Properties properties(String clientId, String clientSecret) {
        return new Sentinel2Properties(
                clientId, clientSecret, "https://token.example", "https://process.example",
                10, 20, 512, 0.2, 500);
    }

    private LandParcel parcel(String reference) {
        var ring = List.of(
                new ParcelGeometry.Coordinate(-8.00, 12.63),
                new ParcelGeometry.Coordinate(-8.00, 12.64),
                new ParcelGeometry.Coordinate(-7.99, 12.64),
                new ParcelGeometry.Coordinate(-8.00, 12.63));
        return LandParcel.reconstitute(
                ParcelId.generate(),
                CadastralReference.of(reference),
                "Parcelle test",
                "Bamako",
                "Moussa Traoré",
                UserId.generate(),
                Hectares.of(1.5),
                MoneyXof.of(10_000_000),
                new ParcelGeometry(ring),
                ParcelStatus.CERTIFIED,
                40, 60, null,
                Instant.now(), Instant.now(), null,
                List.of());
    }
}
