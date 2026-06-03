package com.laboussole.domain.port.out;

import com.laboussole.domain.model.Email;
import com.laboussole.domain.model.User;
import com.laboussole.domain.model.UserId;

import java.util.Optional;

/** Driven port: persistence for the {@link User} aggregate. */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);
}
