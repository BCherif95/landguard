package com.laboussole.domain.port.in;

import com.laboussole.domain.model.User;
import com.laboussole.domain.model.UserId;

/** Driving port: fetch the authenticated principal's profile. */
public interface GetCurrentUserUseCase {

    User execute(UserId userId);
}
