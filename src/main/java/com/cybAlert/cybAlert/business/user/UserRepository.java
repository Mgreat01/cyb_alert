package com.cybAlert.cybAlert.business.user;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    UserEntity save(UserEntity user);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findById(UUID id);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
