package com.cybAlert.cybAlert.application.user;

import com.cybAlert.cybAlert.business.user.UserEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @NotNull UserEntity.Role role,
        @NotNull UserEntity.Status status) {
}
