package com.laboussole.application.usecase.heritage;

import com.laboussole.domain.model.heritage.*;
import com.laboussole.domain.port.in.heritage.VoteSuccessionPlanUseCase;
import com.laboussole.domain.port.out.SuccessionRepository;
import com.laboussole.domain.port.out.heritage.SuccessionAuditEventRepository;
import com.laboussole.domain.port.out.heritage.SuccessionVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class VoteSuccessionPlanService implements VoteSuccessionPlanUseCase {

    private final SuccessionRepository successionRepository;
    private final SuccessionVoteRepository voteRepository;
    private final SuccessionAuditEventRepository auditEventRepository;

    @Override
    @Transactional
    public void vote(SuccessionPlanId planId, HeirId heirId, boolean approved, String ipAddress) {
        SuccessionPlan plan = successionRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Succession plan not found: " + planId));

        plan.castVote(heirId, approved);
        successionRepository.save(plan);

        SuccessionVote vote = SuccessionVote.cast(planId, heirId, approved, ipAddress);
        voteRepository.save(vote);

        SuccessionAuditEvent event = SuccessionAuditEvent.record(
                planId,
                SuccessionAuditType.VOTE_CAST,
                heirId.value().toString(),
                Map.of("approved", String.valueOf(approved), "ip", ipAddress)
        );
        auditEventRepository.save(event);

        if (plan.status() == SuccessionStatus.VALIDATED) {
            auditEventRepository.save(SuccessionAuditEvent.record(
                    planId,
                    SuccessionAuditType.PLAN_VALIDATED,
                    "SYSTEM",
                    Map.of()
            ));
        } else if (plan.status() == SuccessionStatus.REJECTED) {
            auditEventRepository.save(SuccessionAuditEvent.record(
                    planId,
                    SuccessionAuditType.PLAN_REJECTED,
                    "SYSTEM",
                    Map.of()
            ));
        }
    }
}
