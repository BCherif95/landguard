package com.laboussole.application.usecase;

import com.laboussole.domain.exception.EmailAlreadyRegisteredException;
import com.laboussole.domain.model.AuthTokens;
import com.laboussole.domain.model.Email;
import com.laboussole.domain.model.RefreshToken;
import com.laboussole.domain.model.Role;
import com.laboussole.domain.model.User;
import com.laboussole.domain.port.in.RegisterUserUseCase;
import com.laboussole.domain.port.out.PasswordHasher;
import com.laboussole.domain.port.out.RefreshTokenRepository;
import com.laboussole.domain.port.out.TokenIssuer;
import com.laboussole.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public RegisterUserService(
            UserRepository users,
            RefreshTokenRepository refreshTokens,
            PasswordHasher passwordHasher,
            TokenIssuer tokenIssuer) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    @Transactional
    public Result execute(Command command) {
        var email = Email.of(command.email());
        if (users.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        var role = command.role() == null ? Role.CITIZEN : command.role();
        var passwordHash = passwordHasher.hash(command.plaintextPassword());
        var user = User.register(email, command.fullName(), passwordHash, role);
        var saved = users.save(user);

        var tokens = mintTokens(saved);
        return new Result(saved, tokens);
    }

    private AuthTokens mintTokens(User user) {
        var access = tokenIssuer.issueAccessToken(user.id(), user.role());
        var refresh = tokenIssuer.issueRefreshToken();
        var stored = RefreshToken.issue(user.id(), refresh.hash(), tokenIssuer.refreshTokenTtl());
        refreshTokens.save(stored);
        return new AuthTokens(
                access.compactToken(),
                access.expiresAt(),
                refresh.plaintext(),
                stored.expiresAt(),
                user.id());
    }
}
