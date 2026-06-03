package com.laboussole.domain.port.out.heritage;

import com.laboussole.domain.model.heritage.SuccessionVote;
import com.laboussole.domain.model.heritage.SuccessionPlanId;

import java.util.List;

public interface SuccessionVoteRepository {
    void save(SuccessionVote vote);
    List<SuccessionVote> findBySuccessionPlanId(SuccessionPlanId planId);
}
