package com.laboussole.interfaces.rest.notification;

import com.laboussole.domain.port.in.notification.GetNotificationPreferencesUseCase;
import com.laboussole.domain.port.in.notification.UpdateNotificationPreferencesUseCase;
import com.laboussole.infrastructure.security.AuthenticatedPrincipal;
import com.laboussole.interfaces.rest.notification.dto.NotificationPreferencesResponse;
import com.laboussole.interfaces.rest.notification.dto.UpdateNotificationPreferencesRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications/preferences")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications", description = "Per-user alert channel preferences (push, e-mail, SMS).")
class NotificationPreferencesController {

    private final GetNotificationPreferencesUseCase getUseCase;
    private final UpdateNotificationPreferencesUseCase updateUseCase;

    NotificationPreferencesController(
            GetNotificationPreferencesUseCase getUseCase,
            UpdateNotificationPreferencesUseCase updateUseCase) {
        this.getUseCase = getUseCase;
        this.updateUseCase = updateUseCase;
    }

    @Operation(summary = "Current user's alert channel preferences (defaults if never saved).")
    @GetMapping
    public NotificationPreferencesResponse get(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return NotificationPreferencesResponse.from(getUseCase.execute(principal.userId()));
    }

    @Operation(summary = "Update the current user's alert channel preferences.")
    @PutMapping
    public NotificationPreferencesResponse update(
            @Valid @RequestBody UpdateNotificationPreferencesRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        var preferences = updateUseCase.execute(new UpdateNotificationPreferencesUseCase.Command(
                principal.userId(),
                request.pushEnabled(),
                request.emailEnabled(),
                request.smsEnabled(),
                request.phoneNumber()));
        return NotificationPreferencesResponse.from(preferences);
    }
}
