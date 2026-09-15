package com.cybAlert.cybAlert.business.user;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserEntity createUser(String username, String email, String rawPassword);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findById(UUID id);
}
