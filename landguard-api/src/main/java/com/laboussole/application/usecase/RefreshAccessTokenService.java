package com.laboussole.application.usecase;

import com.laboussole.domain.exception.InvalidRefreshTokenException;
import com.laboussole.domain.exception.UserDisabledException;
import com.laboussole.domain.model.AuthTokens;
import com.laboussole.domain.port.in.RefreshAccessTokenUseCase;
import com.laboussole.domain.port.out.TokenIssuer;
import com.laboussole.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stateless refresh: the presented refresh token is a signed JWT verified purely
 * by signature and embedded expiration — no server-side token store is consulted.
 *
 * <p><b>Accepted trade-off:</b> without server-side state there is no way to revoke
 * a refresh token before its natural expiration, and no reuse detection on rotation.
 * In exchange, no token storage or cleanup is needed. Revisit if immediate revocation
 * becomes a requirement (e.g. a Redis-backed denylist keyed by {@code jti}).
 */
@Service
public class RefreshAccessTokenService implements RefreshAccessTokenUseCase {

    private final UserRepository users;
    private final TokenIssuer tokenIssuer;

    public RefreshAccessTokenService(UserRepository users, TokenIssuer tokenIssuer) {
        this.users = users;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthTokens execute(Command command) {
        var verified = tokenIssuer.verifyRefreshToken(command.refreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("invalid-or-expired"));

        // The user is re-loaded so a disabled account stops refreshing immediately,
        // even though the token itself cannot be revoked.
        var user = users.findById(verified.userId())
                .orElseThrow(() -> new InvalidRefreshTokenException("orphaned"));
        if (!user.canAuthenticate()) {
            throw new UserDisabledException();
        }

        var access = tokenIssuer.issueAccessToken(user.id(), user.role());
        var refresh = tokenIssuer.issueRefreshToken(user.id(), user.role());
        return new AuthTokens(
                access.compactToken(),
                access.expiresAt(),
                refresh.compactToken(),
                refresh.expiresAt(),
                user.id());
    }
}
