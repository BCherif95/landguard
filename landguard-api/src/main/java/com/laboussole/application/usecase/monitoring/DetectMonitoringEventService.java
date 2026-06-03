package com.laboussole.application.usecase.monitoring;

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

    public DetectMonitoringEventService(MonitoringRepository repository, MonitoringEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
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
        return event;
    }
}
