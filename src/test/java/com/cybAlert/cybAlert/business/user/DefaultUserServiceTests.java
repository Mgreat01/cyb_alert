package com.cybAlert.cybAlert.business.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultUserServiceTests {

    private UserRepository repository;
    private DefaultUserService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        service = new DefaultUserService(repository, new BCryptPasswordEncoder(4));
    }

    @Test
    void createsANormalizedUserWithAHashedPassword() {
        when(repository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity user = service.createUser(
                " analyst ", " Analyst@CyberWatch.Test ", "plain-password", "Ada", "Lovelace");

        assertThat(user.getUsername()).isEqualTo("analyst");
        assertThat(user.getEmail()).isEqualTo("analyst@cyberwatch.test");
        assertThat(user.getPasswordHash()).isNotEqualTo("plain-password");
        assertThat(user.getFirstName()).isEqualTo("Ada");
        assertThat(new BCryptPasswordEncoder().matches("plain-password", user.getPasswordHash()))
                .isTrue();
    }

    @Test
    void rejectsAnExistingEmail() {
        when(repository.existsByEmail("analyst@cyberwatch.test")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(
                "analyst", "ANALYST@cyberwatch.test", "plain-password", null, null))
                .isInstanceOf(DefaultUserService.UserAlreadyExistsException.class)
                .hasMessageContaining("e-mail");
        verify(repository, never()).save(any());
    }

    @Test
    void findsAUserByNormalizedEmail() {
        UserEntity expected = new UserEntity("analyst", "analyst@cyberwatch.test", "hash");
        when(repository.findByEmail("analyst@cyberwatch.test"))
                .thenReturn(Optional.of(expected));

        assertThat(service.findByEmail(" ANALYST@CyberWatch.Test ")).contains(expected);
    }
}
