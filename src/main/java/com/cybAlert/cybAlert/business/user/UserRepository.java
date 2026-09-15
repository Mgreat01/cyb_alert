package com.cybAlert.cybAlert.business.user;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    UserEntity save(UserEntity user);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findById(UUID id);
}
