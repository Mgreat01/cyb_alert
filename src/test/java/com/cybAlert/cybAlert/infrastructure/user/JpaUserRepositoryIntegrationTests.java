package com.cybAlert.cybAlert.infrastructure.user;

import com.cybAlert.cybAlert.business.user.UserEntity;
import com.cybAlert.cybAlert.business.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class JpaUserRepositoryIntegrationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private UserRepository repository;

    @Test
    void savesAndFindsAUserWithPostgresql() {
        UserEntity user = repository.save(new UserEntity(
                "analyst", "analyst@cyberwatch.test", "encoded-password"));

        assertThat(user.getId()).isNotNull();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(repository.findByEmail("ANALYST@cyberwatch.test"))
                .contains(user);
    }
}
