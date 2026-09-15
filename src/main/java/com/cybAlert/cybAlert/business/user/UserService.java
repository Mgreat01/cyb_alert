package com.cybAlert.cybAlert.business.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserEntity createUser(String username, String email, String rawPassword,
                          String firstName, String lastName);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findById(UUID id);
    Page<UserEntity> findAll(Pageable pageable);
    UserEntity updateUser(UUID id, String firstName, String lastName,
                          UserEntity.Role role, UserEntity.Status status);
    void deleteUser(UUID id);
}
