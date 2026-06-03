package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.RefreshToken;
import com.laboussole.domain.model.RefreshTokenId;
import com.laboussole.domain.model.UserId;
import com.laboussole.domain.port.out.RefreshTokenRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenSpringDataRepository jpa;

    RefreshTokenRepositoryAdapter(RefreshTokenSpringDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        var existing = jpa.findById(token.id().value()).orElse(null);
        if (existing == null) {
            var entity = new RefreshTokenJpaEntity(
                    token.id().value(),
                    token.userId().value(),
                    token.tokenHash(),
                    token.issuedAt(),
                    token.expiresAt(),
                    token.revokedAt().orElse(null),
                    token.replacedBy().map(RefreshTokenId::value).orElse(null));
            return toDomain(jpa.save(entity));
        }
        existing.setRevokedAt(token.revokedAt().orElse(null));
        existing.setReplacedBy(token.replacedBy().map(RefreshTokenId::value).orElse(null));
        return toDomain(jpa.save(existing));
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash).map(RefreshTokenRepositoryAdapter::toDomain);
    }

    @Override
    public void revokeAllForUser(UserId userId) {
        jpa.revokeAllForUser(userId.value(), Instant.now());
    }

    private static RefreshToken toDomain(RefreshTokenJpaEntity e) {
        return RefreshToken.reconstitute(
                RefreshTokenId.of(e.getId()),
                UserId.of(e.getUserId()),
                e.getTokenHash(),
                e.getIssuedAt(),
                e.getExpiresAt(),
                e.getRevokedAt(),
                e.getReplacedBy() == null ? null : RefreshTokenId.of(e.getReplacedBy()));
    }
}
