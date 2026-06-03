package com.laboussole.application.usecase.heritage;

import com.laboussole.domain.model.heritage.SuccessionAuditEvent;
import com.laboussole.domain.model.heritage.SuccessionAuditType;
import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.model.heritage.SuccessionPlanId;
import com.laboussole.domain.port.in.heritage.SubmitSuccessionForVotingUseCase;
import com.laboussole.domain.port.out.SuccessionRepository;
import com.laboussole.domain.port.out.heritage.SuccessionAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubmitSuccessionForVotingService implements SubmitSuccessionForVotingUseCase {

    private final SuccessionRepository successionRepository;
    private final SuccessionAuditEventRepository auditEventRepository;

    @Override
    @Transactional
    public void submit(SuccessionPlanId planId, String actor) {
        SuccessionPlan plan = successionRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Succession plan not found: " + planId));

        plan.submitForVoting();
        successionRepository.save(plan);

        SuccessionAuditEvent event = SuccessionAuditEvent.record(
                planId,
                SuccessionAuditType.SUBMITTED_FOR_VOTING,
                actor,
                Map.of("status", plan.status().name())
        );
        auditEventRepository.save(event);
    }
}
