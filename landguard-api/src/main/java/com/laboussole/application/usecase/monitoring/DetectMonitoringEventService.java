package com.laboussole.application.usecase.monitoring;

import com.laboussole.application.service.AlertEnqueueService;
import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.port.in.DetectMonitoringEventUseCase;
import com.laboussole.domain.port.out.MonitoringEventPublisher;
import com.laboussole.domain.port.out.MonitoringRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DetectMonitoringEventService implements DetectMonitoringEventUseCase {

    private final MonitoringRepository repository;
    private final MonitoringEventPublisher publisher;
    private final AlertEnqueueService alertEnqueue;

    public DetectMonitoringEventService(
            MonitoringRepository repository,
            MonitoringEventPublisher publisher,
            AlertEnqueueService alertEnqueue) {
        this.repository = repository;
        this.publisher = publisher;
        this.alertEnqueue = alertEnqueue;
    }

    @Override
    @Transactional
    public MonitoringEvent execute(Command command) {
        var event = MonitoringEvent.create(
                command.parcelId(),
                command.type(),
                command.severity(),
                command.confidenceScore(),
                command.coordinates(),
                command.description(),
                command.source(),
                command.imageUrl()
        );
        repository.save(event);
        publisher.publish(event);
        // Queued, not sent: the delivery intents commit with the event, so a
        // rollback cannot leave a phantom SMS already on its way.
        alertEnqueue.enqueue(event);
        return event;
    }
}
