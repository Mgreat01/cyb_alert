package com.cybAlert.cybAlert.business.auth;

import com.cybAlert.cybAlert.business.user.UserEntity;
import com.cybAlert.cybAlert.business.audit.AuditService;
import com.cybAlert.cybAlert.business.user.UserRepository;
import com.cybAlert.cybAlert.infrastructure.auth.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthServiceTests {

    private UserRepository users;
    private RefreshTokenRepository refreshTokens;
    private JwtTokenService jwtTokens;
    private AuditService audit;
    private AuthService service;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        refreshTokens = mock(RefreshTokenRepository.class);
        jwtTokens = mock(JwtTokenService.class);
        audit = mock(AuditService.class);
        service = new AuthService(users, refreshTokens, new BCryptPasswordEncoder(4),
                jwtTokens, Duration.ofDays(7), audit);
    }

    @Test
    void locksAnAccountAfterFiveFailedLogins() {
        UserEntity user = new UserEntity("analyst", "analyst@cyberwatch.test",
                new BCryptPasswordEncoder(4).encode("correct-password"));
        when(users.findByEmail("analyst@cyberwatch.test")).thenReturn(Optional.of(user));

        for (int attempt = 0; attempt < 5; attempt++) {
            assertThatThrownBy(() -> service.login(
                    "analyst@cyberwatch.test", "wrong-password"))
                    .isInstanceOf(AuthService.InvalidCredentialsException.class);
        }

        assertThat(user.getStatus()).isEqualTo(UserEntity.Status.LOCKED);
        assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(user.getLockedAt()).isNotNull();
        verify(users, org.mockito.Mockito.times(5)).save(user);
    }

    @Test
    void rejectsAnUnknownRefreshToken() {
        assertThatThrownBy(() -> service.refresh("unknown-token"))
                .isInstanceOf(AuthService.InvalidRefreshTokenException.class);
    }

    @Test
    void issuesTokensAndAuditsSuccessfulLogin() {
        UserEntity user = new UserEntity("analyst", "analyst@cyberwatch.test",
                new BCryptPasswordEncoder(4).encode("correct-password"));
        when(users.findByEmail("analyst@cyberwatch.test")).thenReturn(Optional.of(user));
        when(jwtTokens.createAccessToken(user)).thenReturn("signed-access-token");
        when(jwtTokens.accessTokenExpiresInSeconds()).thenReturn(900L);

        AuthService.TokenPair tokens = service.login(" ANALYST@CyberWatch.Test ",
                "correct-password");

        assertThat(tokens.accessToken()).isEqualTo("signed-access-token");
        assertThat(tokens.refreshToken()).isNotBlank();
        assertThat(tokens.expiresIn()).isEqualTo(900L);
        verify(refreshTokens).save(any(RefreshTokenEntity.class));
        verify(audit).record(any(), org.mockito.Mockito.eq("LOGIN_SUCCESS"),
                org.mockito.Mockito.eq("USER"), any());
    }

    @Test
    void refreshRevokesOldTokenBeforeIssuingAnother() {
        UserEntity user = new UserEntity("analyst", "analyst@cyberwatch.test",
                new BCryptPasswordEncoder(4).encode("correct-password"));
        RefreshTokenEntity oldToken = new RefreshTokenEntity(user, "hash",
                Instant.now().plus(Duration.ofDays(1)));
        when(refreshTokens.findByTokenHash(any())).thenReturn(Optional.of(oldToken));
        when(jwtTokens.createAccessToken(user)).thenReturn("new-access-token");

        AuthService.TokenPair tokens = service.refresh("raw-refresh-token");

        assertThat(tokens.accessToken()).isEqualTo("new-access-token");
        assertThat(oldToken.isUsableAt(Instant.now())).isFalse();
        verify(refreshTokens, org.mockito.Mockito.times(2)).save(any(RefreshTokenEntity.class));
    }

    @Test
    void logoutRevokesKnownRefreshToken() {
        UserEntity user = new UserEntity("analyst", "analyst@cyberwatch.test",
                new BCryptPasswordEncoder(4).encode("correct-password"));
        RefreshTokenEntity token = new RefreshTokenEntity(user, "hash",
                Instant.now().plus(Duration.ofDays(1)));
        when(refreshTokens.findByTokenHash(any())).thenReturn(Optional.of(token));

        service.logout("raw-refresh-token");

        assertThat(token.isUsableAt(Instant.now())).isFalse();
        verify(refreshTokens).save(token);
        verify(audit).record(any(), org.mockito.Mockito.eq("LOGOUT"),
                org.mockito.Mockito.eq("USER"), any());
    }
}
