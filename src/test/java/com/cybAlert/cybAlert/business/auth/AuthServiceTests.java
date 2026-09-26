package com.cybAlert.cybAlert.business.auth;

import com.cybAlert.cybAlert.business.user.UserEntity;
import com.cybAlert.cybAlert.business.audit.AuditService;
import com.cybAlert.cybAlert.business.user.UserRepository;
import com.cybAlert.cybAlert.infrastructure.auth.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTests {

    private UserRepository users;
    private AuthService service;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        service = new AuthService(users, mock(RefreshTokenRepository.class),
                new BCryptPasswordEncoder(4), mock(JwtTokenService.class), Duration.ofDays(7),
                mock(AuditService.class));
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
}
