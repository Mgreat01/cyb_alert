package com.cybAlert.cybAlert.infrastructure.auth;

import com.cybAlert.cybAlert.business.auth.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
}
