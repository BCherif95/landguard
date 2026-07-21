package com.laboussole.application.service;

import com.laboussole.domain.model.Email;
import com.laboussole.domain.model.HashedPassword;
import com.laboussole.domain.model.Role;
import com.laboussole.domain.model.User;
import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.UserStatus;
import com.laboussole.domain.model.heritage.Heir;
import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.model.heritage.SuccessionPlanId;
import com.laboussole.domain.model.heritage.SuccessionStatus;
import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.monitoring.MonitoringEventType;
import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import com.laboussole.domain.model.monitoring.MonitoringSource;
import com.laboussole.domain.model.notification.AlertChannel;
import com.laboussole.domain.model.notification.AlertDelivery;
import com.laboussole.domain.model.notification.AlertDeliveryStatus;
import com.laboussole.domain.model.notification.NotificationPreferences;
import com.laboussole.domain.model.parcel.CadastralReference;
import com.laboussole.domain.model.parcel.Hectares;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.MoneyXof;
import com.laboussole.domain.model.parcel.ParcelGeometry;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.model.parcel.ParcelStatus;
import com.laboussole.domain.port.out.AlertDeliveryRepository;
import com.laboussole.domain.port.out.AlertNotificationPort;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.NotificationPreferencesRepository;
import com.laboussole.domain.port.out.SuccessionRepository;
import com.laboussole.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AlertEnqueueServiceTest {

    @Mock private AlertNotificationPort pushChannel;
    @Mock private AlertNotificationPort emailChannel;
    @Mock private AlertNotificationPort smsChannel;
    @Mock private AlertDeliveryRepository deliveryRepository;
    @Mock private LandParcelRepository parcelRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationPreferencesRepository preferencesRepository;
    @Mock private SuccessionRepository successionRepository;

    private AlertEnqueueService service;

    private final UserId ownerId = UserId.generate();
    private final ParcelId parcelId = ParcelId.generate();
    private User owner;

    @BeforeEach
    void setUp() {
        when(pushChannel.channel()).thenReturn(AlertChannel.PUSH);
        when(emailChannel.channel()).thenReturn(AlertChannel.EMAIL);
        when(smsChannel.channel()).thenReturn(AlertChannel.SMS);

        owner = user(ownerId, "proprietaire@example.ml", "Moussa Traoré");

        lenient().when(parcelRepository.findById(parcelId)).thenReturn(Optional.of(parcel()));
        lenient().when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        lenient().when(successionRepository.findByParcelId(parcelId)).thenReturn(Optional.empty());

        service = new AlertEnqueueService(
                List.of(pushChannel, emailChannel, smsChannel),
                deliveryRepository,
                parcelRepository,
                userRepository,
                preferencesRepository,
                successionRepository);
    }

    @Test
    @DisplayName("a HIGH event queues one delivery per enabled channel")
    void queuesAllEnabledChannels() {
        when(preferencesRepository.findByUserId(ownerId))
                .thenReturn(Optional.of(preferences(true, true, true)));

        int queued = service.enqueue(event(MonitoringSeverity.HIGH));

        assertThat(queued).isEqualTo(3);
        assertThat(capturedChannels())
                .containsExactlyInAnyOrder(AlertChannel.SMS, AlertChannel.PUSH, AlertChannel.EMAIL);
    }

    @Test
    @DisplayName("queued deliveries start PENDING and due immediately — nothing is sent inline")
    void queuedDeliveriesArePendingAndDue() {
        when(preferencesRepository.findByUserId(ownerId))
                .thenReturn(Optional.of(preferences(true, false, false)));

        service.enqueue(event(MonitoringSeverity.CRITICAL));

        assertThat(capturedDeliveries())
                .allSatisfy(delivery -> {
                    assertThat(delivery.status()).isEqualTo(AlertDeliveryStatus.PENDING);
                    assertThat(delivery.attemptCount()).isZero();
                    assertThat(delivery.deliveredAt()).isEmpty();
                });
        verify(pushChannel, never()).dispatch(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("channels disabled in preferences are not queued")
    void skipsDisabledChannels() {
        when(preferencesRepository.findByUserId(ownerId))
                .thenReturn(Optional.of(preferences(true, false, false)));

        service.enqueue(event(MonitoringSeverity.HIGH));

        assertThat(capturedChannels()).containsExactly(AlertChannel.PUSH);
    }

    @Test
    @DisplayName("LOW and MEDIUM severities queue nothing")
    void ignoresLowAndMediumSeverities() {
        assertThat(service.enqueue(event(MonitoringSeverity.LOW))).isZero();
        assertThat(service.enqueue(event(MonitoringSeverity.MEDIUM))).isZero();

        verify(deliveryRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("without saved preferences the defaults apply — push and e-mail, no SMS")
    void defaultsApplyWhenPreferencesNeverSaved() {
        when(preferencesRepository.findByUserId(ownerId)).thenReturn(Optional.empty());

        service.enqueue(event(MonitoringSeverity.HIGH));

        assertThat(capturedChannels())
                .containsExactlyInAnyOrder(AlertChannel.PUSH, AlertChannel.EMAIL);
    }

    @Test
    @DisplayName("a parcel with no owner account queues nothing")
    void queuesNothingWhenParcelHasNoOwnerUser() {
        when(parcelRepository.findById(parcelId)).thenReturn(Optional.of(parcel(null)));

        assertThat(service.enqueue(event(MonitoringSeverity.CRITICAL))).isZero();
        verify(deliveryRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("linked heirs are alerted alongside the owner")
    void alertsLinkedHeirsAlongsideOwner() {
        var heirId = UserId.generate();
        var heirUser = user(heirId, "heritier@example.ml", "Aminata Traoré");
        when(userRepository.findById(heirId)).thenReturn(Optional.of(heirUser));
        when(successionRepository.findByParcelId(parcelId))
                .thenReturn(Optional.of(planWithHeirs(
                        heir("Aminata Traoré", heirId),
                        heir("Héritier sans compte", null))));
        when(preferencesRepository.findByUserId(ownerId))
                .thenReturn(Optional.of(preferences(true, false, false)));
        when(preferencesRepository.findByUserId(heirId))
                .thenReturn(Optional.of(NotificationPreferences.reconstitute(
                        heirId, true, false, false, null, Instant.now())));

        service.enqueue(event(MonitoringSeverity.HIGH));

        assertThat(capturedDeliveries())
                .extracting(AlertDelivery::recipientId)
                .containsExactlyInAnyOrder(ownerId, heirId);
    }

    @Test
    @DisplayName("an owner who is also an heir is queued once, not twice")
    void doesNotQueueTheSameUserTwice() {
        when(successionRepository.findByParcelId(parcelId))
                .thenReturn(Optional.of(planWithHeirs(heir("Moussa Traoré", ownerId))));
        when(preferencesRepository.findByUserId(ownerId))
                .thenReturn(Optional.of(preferences(true, false, false)));

        service.enqueue(event(MonitoringSeverity.HIGH));

        assertThat(capturedDeliveries())
                .extracting(AlertDelivery::recipientId)
                .containsExactly(ownerId);
    }

    @SuppressWarnings("unchecked")
    private List<AlertDelivery> capturedDeliveries() {
        var captor = ArgumentCaptor.forClass(List.class);
        verify(deliveryRepository).saveAll(captor.capture());
        return (List<AlertDelivery>) captor.getValue();
    }

    private List<AlertChannel> capturedChannels() {
        return capturedDeliveries().stream().map(AlertDelivery::channel).toList();
    }

    private static User user(UserId id, String email, String fullName) {
        return User.reconstitute(
                id,
                Email.of(email),
                fullName,
                new HashedPassword("$2a$12$abcdefghijklmnopqrstuv"),
                Role.CITIZEN,
                UserStatus.ACTIVE,
                Instant.now(), Instant.now(), null);
    }

    private Heir heir(String fullName, UserId linkedUserId) {
        return Heir.create(fullName, "Enfant", 50, linkedUserId);
    }

    private SuccessionPlan planWithHeirs(Heir... heirs) {
        return new SuccessionPlan(
                SuccessionPlanId.generate(),
                parcelId,
                List.of(heirs),
                SuccessionStatus.VALIDATED,
                null,
                Instant.now(), Instant.now());
    }

    private NotificationPreferences preferences(boolean push, boolean email, boolean sms) {
        return NotificationPreferences.reconstitute(
                ownerId, push, email, sms, sms ? "+22370000000" : null, Instant.now());
    }

    private MonitoringEvent event(MonitoringSeverity severity) {
        return MonitoringEvent.create(
                parcelId,
                MonitoringEventType.ILLEGAL_OCCUPATION,
                severity,
                90,
                new ParcelGeometry.Coordinate(-8.0029, 12.6392),
                "Occupation détectée",
                MonitoringSource.SATELLITE,
                null);
    }

    private LandParcel parcel() {
        return parcel(ownerId);
    }

    private LandParcel parcel(UserId ownerUserId) {
        var ring = List.of(
                new ParcelGeometry.Coordinate(-8.00, 12.63),
                new ParcelGeometry.Coordinate(-8.00, 12.64),
                new ParcelGeometry.Coordinate(-7.99, 12.64),
                new ParcelGeometry.Coordinate(-8.00, 12.63));
        return LandParcel.reconstitute(
                parcelId,
                CadastralReference.of("BSL-ML-2024-000123"),
                "Parcelle test",
                "Bamako",
                "Moussa Traoré",
                ownerUserId,
                Hectares.of(1.5),
                MoneyXof.of(10_000_000),
                new ParcelGeometry(ring),
                ParcelStatus.CERTIFIED,
                40, 60, null,
                Instant.now(), Instant.now(), null,
                List.of());
    }
}
