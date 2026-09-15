package com.cybAlert.cybAlert.business.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {
    UserEntity save(UserEntity user);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findById(UUID id);
    Page<UserEntity> findAll(Pageable pageable);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    void delete(UserEntity user);
}
