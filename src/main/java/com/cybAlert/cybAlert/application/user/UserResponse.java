package com.cybAlert.cybAlert.application.user;

import com.cybAlert.cybAlert.business.user.UserEntity;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        UserEntity.Role role,
        UserEntity.Status status,
        Instant createdAt,
        Instant updatedAt) {

    public static UserResponse from(UserEntity user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(),
                user.getFirstName(), user.getLastName(), user.getRole(), user.getStatus(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
