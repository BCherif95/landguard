package com.laboussole.application.usecase.heritage;

import com.laboussole.domain.model.heritage.HeirId;
import com.laboussole.domain.model.heritage.SuccessionPlanId;
import com.laboussole.domain.port.in.heritage.ValidateHeirUseCase;
import com.laboussole.domain.port.out.SuccessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidateHeirService implements ValidateHeirUseCase {
    private final SuccessionRepository repository;

    public ValidateHeirService(SuccessionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void execute(SuccessionPlanId planId, HeirId heirId) {
        repository.findById(planId).ifPresent(plan -> {
            plan.castVote(heirId, true);
            repository.save(plan);
        });
    }
}
