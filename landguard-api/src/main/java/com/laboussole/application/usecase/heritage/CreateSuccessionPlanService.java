package com.laboussole.application.usecase.heritage;

import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.port.in.heritage.CreateSuccessionPlanUseCase;
import com.laboussole.domain.port.out.SuccessionRepository;
import com.laboussole.domain.model.parcel.ParcelId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateSuccessionPlanService implements CreateSuccessionPlanUseCase {
    private final SuccessionRepository repository;

    public CreateSuccessionPlanService(SuccessionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public SuccessionPlan execute(ParcelId parcelId) {
        var plan = SuccessionPlan.create(parcelId);
        return repository.save(plan);
    }
}
