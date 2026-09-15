package com.cybAlert.cybAlert.business.auth;

import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshTokenEntity save(RefreshTokenEntity token);

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
}
