package com.laboussole.application.usecase.monitoring;

import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.port.in.ResolveMonitoringEventUseCase;
import com.laboussole.domain.port.out.MonitoringRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResolveMonitoringEventService implements ResolveMonitoringEventUseCase {

    private final MonitoringRepository repository;

    public ResolveMonitoringEventService(MonitoringRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void execute(com.laboussole.domain.model.monitoring.MonitoringEventId id) {
        repository.findById(id).ifPresent(event -> {
            event.resolve();
            repository.save(event);
        });
    }
}
