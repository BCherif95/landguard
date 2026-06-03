package com.laboussole.domain.port.out;

import com.laboussole.domain.model.RefreshToken;
import com.laboussole.domain.model.UserId;

import java.util.Optional;

/** Driven port: persistence for refresh tokens. */
public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Revokes every active token belonging to a user — used on logout-all and on reuse-detection. */
    void revokeAllForUser(UserId userId);
}
