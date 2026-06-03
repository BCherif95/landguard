package com.laboussole.application.usecase.heritage;

import com.laboussole.domain.model.heritage.Heir;
import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.port.in.heritage.AddHeirUseCase;
import com.laboussole.domain.port.out.SuccessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddHeirService implements AddHeirUseCase {
    private final SuccessionRepository repository;

    public AddHeirService(SuccessionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public SuccessionPlan execute(Command command) {
        SuccessionPlan plan = repository.findById(command.planId())
                .orElseThrow(() -> new IllegalArgumentException("Succession plan not found: " + command.planId()));
        
        plan.addHeir(Heir.create(command.fullName(), command.relation(), command.sharePercentage()));
        return repository.save(plan);
    }
}
