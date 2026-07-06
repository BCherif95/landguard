package com.laboussole.application.usecase;

import com.laboussole.domain.exception.InvalidCredentialsException;
import com.laboussole.domain.exception.UserDisabledException;
import com.laboussole.domain.model.AuthTokens;
import com.laboussole.domain.model.Email;
import com.laboussole.domain.model.User;
import com.laboussole.domain.port.in.AuthenticateUserUseCase;
import com.laboussole.domain.port.out.PasswordHasher;
import com.laboussole.domain.port.out.TokenIssuer;
import com.laboussole.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public AuthenticateUserService(
            UserRepository users,
            PasswordHasher passwordHasher,
            TokenIssuer tokenIssuer) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    @Transactional
    public Result execute(Command command) {
        var email = parseEmail(command.email());
        var user = users.findByEmail(email).orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(command.plaintextPassword(), user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        if (!user.canAuthenticate()) {
            throw new UserDisabledException();
        }

        user.recordSuccessfulLogin();
        var saved = users.save(user);

        var tokens = mintTokens(saved);
        return new Result(saved, tokens);
    }

    private Email parseEmail(String raw) {
        try {
            return Email.of(raw);
        } catch (IllegalArgumentException ignored) {
            // Don't leak whether the email is malformed vs unknown — same shape as invalid credentials.
            throw new InvalidCredentialsException();
        }
    }

    private AuthTokens mintTokens(User user) {
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
