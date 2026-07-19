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
import com.laboussole.domain.model.notification.NotificationPreferences;
import com.laboussole.domain.model.parcel.CadastralReference;
import com.laboussole.domain.model.parcel.Hectares;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.MoneyXof;
import com.laboussole.domain.model.parcel.ParcelGeometry;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.model.parcel.ParcelStatus;
import com.laboussole.domain.port.out.AlertNotificationPort;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.NotificationPreferencesRepository;
import com.laboussole.domain.port.out.SuccessionRepository;
import com.laboussole.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MultiChannelAlertRoutingServiceTest {

    @Mock private AlertNotificationPort pushChannel;
    @Mock private AlertNotificationPort emailChannel;
    @Mock private AlertNotificationPort smsChannel;
    @Mock private LandParcelRepository parcelRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationPreferencesRepository preferencesRepository;
    @Mock private SuccessionRepository successionRepository;

    private MultiChannelAlertRoutingService service;

    private final UserId ownerId = UserId.generate();
    private final ParcelId parcelId = ParcelId.generate();
    private User owner;

    @BeforeEach
    void setUp() {
        when(pushChannel.channel()).thenReturn(AlertChannel.PUSH);
        when(emailChannel.channel()).thenReturn(AlertChannel.EMAIL);
        when(smsChannel.channel()).thenReturn(AlertChannel.SMS);

        owner = User.reconstitute(
                ownerId,
                Email.of("proprietaire@example.ml"),
                "Moussa Traoré",
                new HashedPassword("$2a$12$abcdefghijklmnopqrstuv"),
                Role.CITIZEN,
                UserStatus.ACTIVE,
                Instant.now(), Instant.now(), null);

        lenient().when(parcelRepository.findById(parcelId)).thenReturn(Optional.of(parcel()));
        lenient().when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        lenient().when(successionRepository.findByParcelId(parcelId)).thenReturn(Optional.empty());

        // Same-thread executor makes parallel dispatch deterministic in tests.
        service = new MultiChannelAlertRoutingService(
                List.of(pushChannel, emailChannel, smsChannel),
                parcelRepository,
                userRepository,
                preferencesRepository,
                successionRepository,
                Runnable::run);
    }

    @Test
    void dispatchesAllEnabledChannelsForHighSeverity() {
        when(preferencesRepository.findByUserId(ownerId))
                .thenReturn(Optional.of(preferences(true, true, true)));

        service.route(event(MonitoringSeverity.HIGH)).join();

        verify(pushChannel).dispatch(any(), any());
        verify(emailChannel).dispatch(any(), any());
        verify(smsChannel).dispatch(any(), any());
    }

    @Test
    void oneFailingChannelDoesNotPreventTheOthers() {
        when(preferencesRepository.findByUserId(ownerId))
                .thenReturn(Optional.of(preferences(true, true, true)));
        doThrow(new RuntimeException("Twilio is down"))
                .when(smsChannel).dispatch(any(), any());

        service.route(event(MonitoringSeverity.CRITICAL)).join();

        verify(pushChannel).dispatch(any(), any());
        verify(emailChannel).dispatch(any(), any());
    }

    @Test
    void skipsChannelsDisabledInPreferences() {
        when(preferencesRepository.findByUserId(ownerId))
                .thenReturn(Optional.of(preferences(true, false, false)));

        service.route(event(MonitoringSeverity.HIGH)).join();

        verify(pushChannel).dispatch(any(), any());
        verify(emailChannel, never()).dispatch(any(), any());
        verify(smsChannel, never()).dispatch(any(), any());
    }

    @Test
    void ignoresLowAndMediumSeverities() {
        service.route(event(MonitoringSeverity.LOW)).join();
        service.route(event(MonitoringSeverity.MEDIUM)).join();

        verify(pushChannel, never()).dispatch(any(), any());
        verify(emailChannel, never()).dispatch(any(), any());
        verify(smsChannel, never()).dispatch(any(), any());
    }

    @Test
    void defaultsApplyWhenUserNeverSavedPreferences_pushAndEmailOnly() {
        when(preferencesRepository.findByUserId(ownerId)).thenReturn(Optional.empty());

        service.route(event(MonitoringSeverity.HIGH)).join();

        verify(pushChannel).dispatch(any(), any());
        verify(emailChannel).dispatch(any(), any());
        verify(smsChannel, never()).dispatch(any(), any());
    }

    @Test
    void doesNothingWhenParcelHasNoOwnerUser() {
        when(parcelRepository.findById(parcelId)).thenReturn(Optional.of(parcelWithoutOwner()));

        service.route(event(MonitoringSeverity.CRITICAL)).join();

        verify(pushChannel, never()).dispatch(any(), any());
        verify(emailChannel, never()).dispatch(any(), any());
        verify(smsChannel, never()).dispatch(any(), any());
    }

    @Test
    void alertsLinkedHeirsAlongsideTheOwner() {
        var heirId = UserId.generate();
        var heirUser = User.reconstitute(
                heirId,
                Email.of("heritier@example.ml"),
                "Aminata Traoré",
                new HashedPassword("$2a$12$abcdefghijklmnopqrstuv"),
                Role.CITIZEN,
                UserStatus.ACTIVE,
                Instant.now(), Instant.now(), null);
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

        service.route(event(MonitoringSeverity.HIGH)).join();

        verify(pushChannel).dispatch(any(), eq(owner));
        verify(pushChannel).dispatch(any(), eq(heirUser));
    }

    @Test
    void doesNotAlertTheSameUserTwiceWhenOwnerIsAlsoAnHeir() {
        when(successionRepository.findByParcelId(parcelId))
                .thenReturn(Optional.of(planWithHeirs(heir("Moussa Traoré", ownerId))));
        when(preferencesRepository.findByUserId(ownerId))
                .thenReturn(Optional.of(preferences(true, false, false)));

        service.route(event(MonitoringSeverity.HIGH)).join();

        verify(pushChannel, times(1)).dispatch(any(), eq(owner));
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

    private LandParcel parcelWithoutOwner() {
        return parcel(null);
    }

    private LandParcel parcel(UserId owner) {
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
                owner,
                Hectares.of(1.5),
                MoneyXof.of(10_000_000),
                new ParcelGeometry(ring),
                ParcelStatus.CERTIFIED,
                40, 60, null,
                Instant.now(), Instant.now(), null,
                List.of());
    }
}
