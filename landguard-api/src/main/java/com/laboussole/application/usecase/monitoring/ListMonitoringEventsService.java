package com.laboussole.application.usecase.monitoring;

import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.port.in.ListMonitoringEventsUseCase;
import com.laboussole.domain.port.out.MonitoringRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListMonitoringEventsService implements ListMonitoringEventsUseCase {

    private final MonitoringRepository repository;

    public ListMonitoringEventsService(MonitoringRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonitoringEvent> execute(Query query) {
        if (query.parcelId() != null) {
            return repository.findByParcelId(query.parcelId());
        }
        if (query.severity() != null) {
            return repository.findBySeverity(query.severity());
        }
        return repository.findAll();
    }
}
