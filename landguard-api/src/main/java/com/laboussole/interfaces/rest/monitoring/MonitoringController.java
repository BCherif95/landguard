package com.laboussole.interfaces.rest.monitoring;

import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.monitoring.MonitoringEventId;
import com.laboussole.domain.model.monitoring.MonitoringSeverity;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.in.ListMonitoringEventsUseCase;
import com.laboussole.domain.port.in.ResolveMonitoringEventUseCase;
import com.laboussole.infrastructure.monitoring.SseMonitoringEventPublisher;
import com.laboussole.interfaces.rest.monitoring.dto.MonitoringEventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/monitoring")
@Tag(name = "Monitoring", description = "Real-time surveillance and monitoring events")
public class MonitoringController {

    private final ListMonitoringEventsUseCase listEventsUseCase;
    private final ResolveMonitoringEventUseCase resolveEventUseCase;
    private final SseMonitoringEventPublisher ssePublisher;

    public MonitoringController(
            ListMonitoringEventsUseCase listEventsUseCase,
            ResolveMonitoringEventUseCase resolveEventUseCase,
            SseMonitoringEventPublisher ssePublisher) {
        this.listEventsUseCase = listEventsUseCase;
        this.resolveEventUseCase = resolveEventUseCase;
        this.ssePublisher = ssePublisher;
    }

    @GetMapping("/events")
    @Operation(summary = "List monitoring events")
    public List<MonitoringEventResponse> listEvents(
            @RequestParam(required = false) UUID parcelId,
            @RequestParam(required = false) MonitoringSeverity severity) {
        var query = new ListMonitoringEventsUseCase.Query(
                parcelId != null ? new ParcelId(parcelId) : null,
                severity
        );
        return listEventsUseCase.execute(query).stream()
                .map(MonitoringEventResponse::fromDomain)
                .collect(Collectors.toList());
    }

    @PostMapping("/events/{id}/resolve")
    @Operation(summary = "Resolve a monitoring event")
    public void resolveEvent(@PathVariable UUID id) {
        resolveEventUseCase.execute(new MonitoringEventId(id));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Real-time monitoring events stream (SSE)")
    public SseEmitter streamEvents() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Authentification requise pour le flux de surveillance");
        }
        return ssePublisher.subscribe();
    }
}
