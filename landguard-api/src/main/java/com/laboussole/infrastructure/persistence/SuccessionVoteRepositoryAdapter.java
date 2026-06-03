package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.heritage.HeirId;
import com.laboussole.domain.model.heritage.SuccessionPlanId;
import com.laboussole.domain.model.heritage.SuccessionVote;
import com.laboussole.domain.port.out.heritage.SuccessionVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SuccessionVoteRepositoryAdapter implements SuccessionVoteRepository {

    private final SuccessionVoteSpringDataRepository repository;

    @Override
    public void save(SuccessionVote vote) {
        SuccessionVoteJpaEntity entity = new SuccessionVoteJpaEntity(
                vote.id(),
                vote.successionPlanId().value(),
                vote.heirId().value(),
                vote.approved(),
                vote.votedAt(),
                vote.ipAddress()
        );
        repository.save(entity);
    }

    @Override
    public List<SuccessionVote> findBySuccessionPlanId(SuccessionPlanId planId) {
        return repository.findBySuccessionPlanId(planId.value()).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private SuccessionVote mapToDomain(SuccessionVoteJpaEntity entity) {
        return new SuccessionVote(
                entity.getId(),
                SuccessionPlanId.of(entity.getSuccessionPlanId()),
                HeirId.of(entity.getHeirId()),
                entity.isApproved(),
                entity.getVotedAt(),
                entity.getIpAddress()
        );
    }
}
