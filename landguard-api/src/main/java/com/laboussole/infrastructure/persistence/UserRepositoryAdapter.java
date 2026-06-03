package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.Email;
import com.laboussole.domain.model.HashedPassword;
import com.laboussole.domain.model.User;
import com.laboussole.domain.model.UserId;
import com.laboussole.domain.port.out.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class UserRepositoryAdapter implements UserRepository {

    private final UserSpringDataRepository jpa;

    UserRepositoryAdapter(UserSpringDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public User save(User user) {
        var existing = jpa.findById(user.id().value()).orElse(null);
        var entity = existing != null ? existing : new UserJpaEntity(
                user.id().value(),
                user.email().value(),
                user.fullName(),
                user.passwordHash().value(),
                user.role(),
                user.status(),
                user.createdAt(),
                user.updatedAt(),
                user.lastLoginAt());
        if (existing != null) {
            entity.setEmail(user.email().value());
            entity.setFullName(user.fullName());
            entity.setPasswordHash(user.passwordHash().value());
            entity.setRole(user.role());
            entity.setStatus(user.status());
            entity.setUpdatedAt(user.updatedAt());
            entity.setLastLoginAt(user.lastLoginAt());
        }
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpa.findById(id.value()).map(UserRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpa.findByEmail(email.value()).map(UserRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpa.existsByEmail(email.value());
    }

    private static User toDomain(UserJpaEntity e) {
        return User.reconstitute(
                UserId.of(e.getId()),
                Email.of(e.getEmail()),
                e.getFullName(),
                new HashedPassword(e.getPasswordHash()),
                e.getRole(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getLastLoginAt());
    }
}
