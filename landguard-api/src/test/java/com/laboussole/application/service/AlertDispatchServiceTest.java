package com.laboussole.application.service;

import com.laboussole.domain.model.Email;
import com.laboussole.domain.model.HashedPassword;
import com.laboussole.domain.model.Role;
import com.laboussole.domain.model.User;
import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.UserStatus;
import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.monitoring.MonitoringEventType;
import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import com.laboussole.domain.model.monitoring.MonitoringSource;
import com.laboussole.domain.model.notification.AlertChannel;
import com.laboussole.domain.model.notification.AlertDelivery;
import com.laboussole.domain.model.notification.AlertDeliveryStatus;
import com.laboussole.domain.model.parcel.ParcelGeometry;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.out.AlertDeliveryRepository;
import com.laboussole.domain.port.out.AlertNotificationPort;
import com.laboussole.domain.port.out.MonitoringRepository;
import com.laboussole.domain.port.out.UserRepository;
import com.laboussole.infrastructure.notification.AlertDispatchProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AlertDispatchServiceTest {

    private static final AlertDispatchProperties PROPERTIES = new AlertDispatchProperties(
            50, 3, Duration.ofMinutes(1), Duration.ofMinutes(10));

    @Mock private AlertNotificationPort smsChannel;
    @Mock private AlertNotificationPort emailChannel;
    @Mock private AlertDeliveryRepository deliveries;
    @Mock private MonitoringRepository monitoringRepository;
    @Mock private UserRepository userRepository;

    private AlertDispatchService service;

    private final UserId recipientId = UserId.generate();
    private final ParcelId parcelId = ParcelId.generate();
    private MonitoringEvent event;
    private User recipient;

    @BeforeEach
    void setUp() {
        when(smsChannel.channel()).thenReturn(AlertChannel.SMS);
        when(emailChannel.channel()).thenReturn(AlertChannel.EMAIL);

        event = MonitoringEvent.create(
                parcelId, MonitoringEventType.ILLEGAL_OCCUPATION, MonitoringSeverity.CRITICAL,
                95, new ParcelGeometry.Coordinate(-8.0029, 12.6392),
                "Occupation détectée", MonitoringSource.SATELLITE, null);
        recipient = User.reconstitute(
                recipientId, Email.of("proprietaire@example.ml"), "Moussa Traoré",
                new HashedPassword("$2a$12$abcdefghijklmnopqrstuv"),
                Role.CITIZEN, UserStatus.ACTIVE, Instant.now(), Instant.now(), null);

        lenient().when(monitoringRepository.findById(event.id())).thenReturn(Optional.of(event));
        lenient().when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipient));
        lenient().when(deliveries.findStaleInFlight(any(), anyInt())).thenReturn(List.of());

        service = new AlertDispatchService(
                List.of(smsChannel, emailChannel),
                deliveries, monitoringRepository, userRepository, PROPERTIES);
    }

    private AlertDelivery claimed(AlertChannel channel) {
        var delivery = AlertDelivery.queue(event.id(), recipientId, channel, Instant.now());
        delivery.markInFlight(Instant.now());
        return delivery;
    }

    @Test
    @DisplayName("a successful dispatch marks the delivery DELIVERED")
    void successfulDispatchIsRecorded() {
        var delivery = claimed(AlertChannel.SMS);
        when(deliveries.claimDueForDispatch(anyInt(), any())).thenReturn(List.of(delivery));

        assertThat(service.drain()).isEqualTo(1);

        verify(smsChannel).dispatch(event, recipient);
        assertThat(delivery.status()).isEqualTo(AlertDeliveryStatus.DELIVERED);
        verify(deliveries).save(delivery);
    }

    @Test
    @DisplayName("a failing channel schedules a retry instead of dropping the alert")
    void failureSchedulesRetry() {
        var delivery = claimed(AlertChannel.SMS);
        when(deliveries.claimDueForDispatch(anyInt(), any())).thenReturn(List.of(delivery));
        doThrow(new RuntimeException("Twilio is down")).when(smsChannel).dispatch(any(), any());

        service.drain();

        assertThat(delivery.status()).isEqualTo(AlertDeliveryStatus.PENDING);
        assertThat(delivery.lastError()).contains("Twilio is down");
        assertThat(delivery.nextAttemptAt()).isAfter(Instant.now());
        verify(deliveries).save(delivery);
    }

    @Test
    @DisplayName("one failing delivery never aborts the rest of the batch")
    void failureIsIsolatedFromTheBatch() {
        var failing = claimed(AlertChannel.SMS);
        var healthy = claimed(AlertChannel.EMAIL);
        when(deliveries.claimDueForDispatch(anyInt(), any())).thenReturn(List.of(failing, healthy));
        doThrow(new RuntimeException("Twilio is down")).when(smsChannel).dispatch(any(), any());

        assertThat(service.drain()).isEqualTo(2);

        verify(emailChannel).dispatch(event, recipient);
        assertThat(healthy.status()).isEqualTo(AlertDeliveryStatus.DELIVERED);
        assertThat(failing.status()).isEqualTo(AlertDeliveryStatus.PENDING);
    }

    @Test
    @DisplayName("the alert is dead-lettered once the attempt budget is exhausted")
    void exhaustedDeliveryIsDeadLettered() {
        var delivery = AlertDelivery.queue(event.id(), recipientId, AlertChannel.SMS, Instant.now());
        // Burn every attempt but the last.
        for (int i = 1; i < PROPERTIES.maxAttempts(); i++) {
            delivery.markInFlight(Instant.now());
            delivery.recordFailure("down", PROPERTIES.maxAttempts(),
                    PROPERTIES.baseBackoff(), Instant.now());
        }
        delivery.markInFlight(Instant.now());
        when(deliveries.claimDueForDispatch(anyInt(), any())).thenReturn(List.of(delivery));
        doThrow(new RuntimeException("Twilio is down")).when(smsChannel).dispatch(any(), any());

        service.drain();

        assertThat(delivery.status()).isEqualTo(AlertDeliveryStatus.DEAD_LETTER);
        assertThat(delivery.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("a delivery whose event vanished fails instead of throwing")
    void missingEventFailsGracefully() {
        var delivery = claimed(AlertChannel.SMS);
        when(deliveries.claimDueForDispatch(anyInt(), any())).thenReturn(List.of(delivery));
        when(monitoringRepository.findById(event.id())).thenReturn(Optional.empty());

        service.drain();

        verify(smsChannel, never()).dispatch(any(), any());
        assertThat(delivery.lastError()).isPresent();
        assertThat(delivery.status()).isEqualTo(AlertDeliveryStatus.PENDING);
    }

    @Test
    @DisplayName("an empty queue does no work")
    void emptyQueueIsANoOp() {
        when(deliveries.claimDueForDispatch(anyInt(), any())).thenReturn(List.of());

        assertThat(service.drain()).isZero();

        verify(smsChannel, never()).dispatch(any(), any());
        verify(deliveries, never()).save(any());
    }

    @Test
    @DisplayName("deliveries abandoned by a dead worker are returned to the queue")
    void staleClaimsAreRequeued() {
        var stale = claimed(AlertChannel.SMS);
        when(deliveries.findStaleInFlight(any(), anyInt())).thenReturn(List.of(stale));
        when(deliveries.claimDueForDispatch(anyInt(), any())).thenReturn(List.of());

        service.drain();

        assertThat(stale.status()).isEqualTo(AlertDeliveryStatus.PENDING);
        verify(deliveries).save(stale);
    }

    @Test
    @DisplayName("the batch size configured is the one requested from the queue")
    void batchSizeIsHonoured() {
        when(deliveries.claimDueForDispatch(anyInt(), any())).thenReturn(List.of());

        service.drain();

        verify(deliveries).claimDueForDispatch(eq(PROPERTIES.batchSize()), any());
    }

    @Test
    @DisplayName("nothing is saved in bulk — the dispatcher only ever updates single rows")
    void dispatcherNeverBulkSaves() {
        when(deliveries.claimDueForDispatch(anyInt(), any()))
                .thenReturn(List.of(claimed(AlertChannel.SMS)));

        service.drain();

        verify(deliveries, never()).saveAll(anyList());
    }
}
