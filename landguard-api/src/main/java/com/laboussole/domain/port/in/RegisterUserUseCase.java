package com.laboussole.domain.port.in;

import com.laboussole.domain.model.AuthTokens;
import com.laboussole.domain.model.Role;
import com.laboussole.domain.model.User;

/** Driving port: register a new account and immediately authenticate it. */
public interface RegisterUserUseCase {

    Result execute(Command command);

    record Command(String email, String fullName, String plaintextPassword, Role role) {}

    record Result(User user, AuthTokens tokens) {}
}
