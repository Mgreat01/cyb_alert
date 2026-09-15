package com.cybAlert.cybAlert.infrastructure.user;

import com.cybAlert.cybAlert.business.user.UserEntity;
import com.cybAlert.cybAlert.business.user.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository repository;

    public JpaUserRepository(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserEntity save(UserEntity user) {
        return repository.save(user);
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return repository.findByEmailIgnoreCase(email);
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        return repository.findById(id);
    }
}
