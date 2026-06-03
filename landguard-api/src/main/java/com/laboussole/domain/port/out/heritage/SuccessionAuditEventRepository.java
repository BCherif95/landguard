package com.laboussole.domain.port.out.heritage;

import com.laboussole.domain.model.heritage.SuccessionAuditEvent;
import com.laboussole.domain.model.heritage.SuccessionPlanId;

import java.util.List;

public interface SuccessionAuditEventRepository {
    void save(SuccessionAuditEvent event);
    List<SuccessionAuditEvent> findBySuccessionPlanId(SuccessionPlanId planId);
}
