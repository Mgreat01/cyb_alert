package com.cybAlert.cybAlert.business.user;

import com.cybAlert.cybAlert.business.audit.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultUserServiceTests {

    private UserRepository repository;
    private AuditService audit;
    private DefaultUserService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        audit = mock(AuditService.class);
        service = new DefaultUserService(repository, new BCryptPasswordEncoder(4), audit);
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
        verify(audit).recordCurrentActor("USER_CREATED", "USER", "null");
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

    @Test
    void auditsProfileChangesAndDeletion() {
        UUID id = UUID.randomUUID();
        UserEntity user = new UserEntity("analyst", "analyst@cyberwatch.test", "hash");
        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        service.updateUser(id, "Ada", "Lovelace", UserEntity.Role.SOC_ANALYST,
                UserEntity.Status.ACTIVE);
        service.deleteUser(id);

        verify(audit).recordCurrentActor("USER_UPDATED", "USER", id.toString());
        verify(audit).recordCurrentActor("USER_DELETED", "USER", id.toString());
    }
}
