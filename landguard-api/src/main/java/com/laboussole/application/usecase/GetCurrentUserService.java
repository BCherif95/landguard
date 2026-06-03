package com.laboussole.application.usecase;

import com.laboussole.domain.exception.UserNotFoundException;
import com.laboussole.domain.model.User;
import com.laboussole.domain.model.UserId;
import com.laboussole.domain.port.in.GetCurrentUserUseCase;
import com.laboussole.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCurrentUserService implements GetCurrentUserUseCase {

    private final UserRepository users;

    public GetCurrentUserService(UserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public User execute(UserId userId) {
        return users.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
}
