package com.laboussole.interfaces.rest.auth.dto;

import com.laboussole.domain.model.Role;
import com.laboussole.domain.model.User;
import com.laboussole.domain.model.UserStatus;

import java.time.Instant;

public record UserResponse(
        String id,
        String email,
        String fullName,
        Role role,
        UserStatus status,
        Instant createdAt,
        Instant lastLoginAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.id().asString(),
                user.email().value(),
                user.fullName(),
                user.role(),
                user.status(),
                user.createdAt(),
                user.lastLoginAt());
    }
}
