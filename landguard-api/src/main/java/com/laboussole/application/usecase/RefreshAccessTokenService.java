package com.laboussole.application.usecase;

import com.laboussole.domain.exception.InvalidRefreshTokenException;
import com.laboussole.domain.exception.UserDisabledException;
import com.laboussole.domain.model.AuthTokens;
import com.laboussole.domain.model.RefreshToken;
import com.laboussole.domain.port.in.RefreshAccessTokenUseCase;
import com.laboussole.domain.port.out.RefreshTokenRepository;
import com.laboussole.domain.port.out.TokenIssuer;
import com.laboussole.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshAccessTokenService implements RefreshAccessTokenUseCase {

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final TokenIssuer tokenIssuer;

    public RefreshAccessTokenService(
            UserRepository users,
            RefreshTokenRepository refreshTokens,
            TokenIssuer tokenIssuer) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    @Transactional
    public AuthTokens execute(Command command) {
        var presentedHash = tokenIssuer.hashRefreshToken(command.refreshToken());
        var stored = refreshTokens.findByTokenHash(presentedHash)
                .orElseThrow(() -> new InvalidRefreshTokenException("unknown"));

        if (stored.isRevoked()) {
            // Reuse-detection: revoke the entire chain for this user.
            refreshTokens.revokeAllForUser(stored.userId());
            throw new InvalidRefreshTokenException("reuse-detected");
        }
        if (!stored.isActive()) {
            throw new InvalidRefreshTokenException("expired");
        }

        var user = users.findById(stored.userId())
                .orElseThrow(() -> new InvalidRefreshTokenException("orphaned"));
        if (!user.canAuthenticate()) {
            throw new UserDisabledException();
        }

        var newRefresh = tokenIssuer.issueRefreshToken();
        var newToken = RefreshToken.issue(user.id(), newRefresh.hash(), tokenIssuer.refreshTokenTtl());
        var savedNew = refreshTokens.save(newToken);

        stored.revoke(savedNew.id());
        refreshTokens.save(stored);

        var access = tokenIssuer.issueAccessToken(user.id(), user.role());
        return new AuthTokens(
                access.compactToken(),
                access.expiresAt(),
                newRefresh.plaintext(),
                savedNew.expiresAt(),
                user.id());
    }
}
