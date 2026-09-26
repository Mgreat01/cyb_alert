package com.cybAlert.cybAlert.application.event;

import com.cybAlert.cybAlert.business.audit.AuditService;
import com.cybAlert.cybAlert.business.event.SecurityEvent;
import com.cybAlert.cybAlert.business.event.SecurityEventService;
import com.cybAlert.cybAlert.business.webhook.WebhookVerifier;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WebhookControllerTests {

    @Test
    void verifiesRawBodyBeforeIngestionAndAuditsAcceptance() {
        WebhookVerifier verifier = mock(WebhookVerifier.class);
        SecurityEventService events = mock(SecurityEventService.class);
        AuditService audit = mock(AuditService.class);
        Validator validator = mock(Validator.class);
        when(validator.validate(any(WebhookController.WebhookEvent.class)))
                .thenReturn(Set.of());
        Instant timestamp = Instant.parse("2026-09-25T10:00:00Z");
        UUID sourceId = UUID.randomUUID();
        byte[] body = ("{\"eventId\":\"evt-1\",\"eventType\":\"LOGIN_FAILED\","
                + "\"timestamp\":\"2026-09-25T10:00:00Z\",\"sourceId\":\"" + sourceId
                + "\",\"severity\":\"HIGH\",\"message\":\"Échec\",\"metadata\":{}}")
                .getBytes(StandardCharsets.UTF_8);
        when(events.ingest("evt-1", "LOGIN_FAILED", timestamp, sourceId, null, null,
                null, null, null, null, "HIGH", "Échec", Map.of()))
                .thenReturn(new SecurityEvent("evt-1", "LOGIN_FAILED", timestamp, sourceId,
                        null, null, null, null, null, null, "HIGH", "Échec", Map.of()));
        WebhookController controller = new WebhookController(verifier, events, audit,
                JsonMapper.builder().build(), validator);

        WebhookController.WebhookResponse response = controller.accept("123", "nonce",
                "signature", body);

        assertThat(response.eventId()).isEqualTo("evt-1");
        verify(verifier).verify("123", "nonce", "signature", body);
        verify(audit).record(null, "WEBHOOK_ACCEPTED", "EVENT", "evt-1");
    }

    @Test
    void rejectsMalformedJsonAfterSignatureCheck() {
        WebhookVerifier verifier = mock(WebhookVerifier.class);
        SecurityEventService events = mock(SecurityEventService.class);
        AuditService audit = mock(AuditService.class);
        WebhookController controller = new WebhookController(verifier, events, audit,
                JsonMapper.builder().build(), mock(Validator.class));
        byte[] body = "{".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> controller.accept("123", "nonce", "signature", body))
                .isInstanceOf(WebhookController.InvalidWebhookPayloadException.class);
        verify(verifier).verify("123", "nonce", "signature", body);
        verifyNoInteractions(events);
    }
}
