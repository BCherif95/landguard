package com.laboussole.domain.port.in;

import com.laboussole.domain.model.AuthTokens;
import com.laboussole.domain.model.User;

/** Driving port: e-mail + password authentication. */
public interface AuthenticateUserUseCase {

    Result execute(Command command);

    record Command(String email, String plaintextPassword) {}

    record Result(User user, AuthTokens tokens) {}
}
