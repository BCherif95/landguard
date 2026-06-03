package com.laboussole.application.usecase;

import com.laboussole.domain.port.in.LogoutUseCase;
import com.laboussole.domain.port.out.RefreshTokenRepository;
import com.laboussole.domain.port.out.TokenIssuer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenRepository refreshTokens;
    private final TokenIssuer tokenIssuer;

    public LogoutService(RefreshTokenRepository refreshTokens, TokenIssuer tokenIssuer) {
        this.refreshTokens = refreshTokens;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    @Transactional
    public void execute(Command command) {
        if (command.refreshToken() == null || command.refreshToken().isBlank()) {
            return;
        }
        var hash = tokenIssuer.hashRefreshToken(command.refreshToken());
        refreshTokens.findByTokenHash(hash).ifPresent(token -> {
            if (!token.isRevoked()) {
                token.revoke();
                refreshTokens.save(token);
            }
        });
    }
}
