package com.laboussole.infrastructure.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.port.out.MonitoringEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class SseMonitoringEventPublisher implements MonitoringEventPublisher {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;

    public SseMonitoringEventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(24 * 60 * 60 * 1000L); // 24h timeout
        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError((e) -> this.emitters.remove(emitter));

        // Send a heartbeat or init event
        synchronized (emitter) {
            try {
                emitter.send(SseEmitter.event().name("init").data("connected"));
            } catch (Exception e) {
                log.error("Error sending init event", e);
                emitter.complete();
                this.emitters.remove(emitter);
            }
        }

        return emitter;
    }

    @Override
    public void publish(MonitoringEvent event) {
        this.emitters.forEach(emitter -> {
            synchronized (emitter) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("monitoring-event")
                            .data(event));
                } catch (Exception e) {
                    log.warn("Failed to send event to emitter, removing it", e);
                    try {
                        emitter.complete();
                    } catch (Exception ignored) {}
                    this.emitters.remove(emitter);
                }
            }
        });
    }

    @Scheduled(fixedRate = 20000) // Every 20 seconds
    public void sendHeartbeat() {
        if (emitters.isEmpty()) return;
        
        this.emitters.forEach(emitter -> {
            synchronized (emitter) {
                try {
                    emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
                } catch (Exception e) {
                    this.emitters.remove(emitter);
                }
            }
        });
    }
}
