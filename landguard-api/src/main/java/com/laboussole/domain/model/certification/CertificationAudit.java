package com.laboussole.domain.model.certification;

import com.laboussole.domain.model.UserId;

import java.time.Instant;
import java.util.UUID;

public record CertificationAudit(
        UUID id,
        CertificationId certificationId,
        String action,
        UserId actor,
        Instant timestamp,
        String decision,
        String notes,
        String ipAddress
) {
    public static CertificationAudit log(
            CertificationId certId,
            String action,
            UserId actor,
            String decision,
            String notes,
            String ip) {
        return new CertificationAudit(
                UUID.randomUUID(),
                certId,
                action,
                actor,
                Instant.now(),
                decision,
                notes,
                ip
        );
    }
}
