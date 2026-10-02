package com.cybAlert.cybAlert.business.audit;

import com.cybAlert.cybAlert.infrastructure.audit.SpringDataAuditLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuditServiceTests {

    @Test
    void recordsActionAndResourceWithoutCredentials() {
        SpringDataAuditLogRepository logs = mock(SpringDataAuditLogRepository.class);
        UUID userId = UUID.randomUUID();

        new AuditService(logs).record(userId, "LOGIN_SUCCESS", "USER", userId.toString());

        ArgumentCaptor<AuditLogEntity> saved = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(logs).save(saved.capture());
        assertThat(saved.getValue().getUserId()).isEqualTo(userId);
        assertThat(saved.getValue().getAction()).isEqualTo("LOGIN_SUCCESS");
        assertThat(saved.getValue().getResourceId()).isEqualTo(userId.toString());
        assertThat(saved.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    void attributesSensitiveActionToAuthenticatedJwtSubject() {
        SpringDataAuditLogRepository logs = mock(SpringDataAuditLogRepository.class);
        UUID actorId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("test-token").header("alg", "HS256")
                .subject(actorId.toString()).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        try {
            new AuditService(logs).recordCurrentActor("SOURCE_DELETED", "SOURCE", "source-1");
            ArgumentCaptor<AuditLogEntity> saved = ArgumentCaptor.forClass(AuditLogEntity.class);
            verify(logs).save(saved.capture());
            assertThat(saved.getValue().getUserId()).isEqualTo(actorId);
            assertThat(saved.getValue().getAction()).isEqualTo("SOURCE_DELETED");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
