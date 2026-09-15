package com.cybAlert.cybAlert.infrastructure.auth;

import com.cybAlert.cybAlert.business.auth.RefreshTokenEntity;
import com.cybAlert.cybAlert.business.auth.RefreshTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaRefreshTokenRepository implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository repository;

    public JpaRefreshTokenRepository(SpringDataRefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public RefreshTokenEntity save(RefreshTokenEntity token) {
        return repository.save(token);
    }

    @Override
    public Optional<RefreshTokenEntity> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash);
    }
}
