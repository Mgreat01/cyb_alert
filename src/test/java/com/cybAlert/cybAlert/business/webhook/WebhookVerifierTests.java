package com.cybAlert.cybAlert.business.webhook;

import com.cybAlert.cybAlert.business.audit.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebhookVerifierTests {

    private static final String SECRET = "webhook-secret-with-at-least-32-bytes";
    private StringRedisTemplate redis;
    private ValueOperations<String, String> values;
    private AuditService audit;
    private WebhookVerifier verifier;

    @BeforeEach
    void setUp() {
        redis = mock(StringRedisTemplate.class);
        values = mock(ValueOperations.class);
        audit = mock(AuditService.class);
        when(redis.opsForValue()).thenReturn(values);
        verifier = new WebhookVerifier(redis, audit, SECRET);
    }

    @Test
    void acceptsValidSignatureAndClaimsNonce() throws Exception {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = UUID.randomUUID().toString();
        byte[] body = "{\"eventId\":\"evt-1\"}".getBytes(StandardCharsets.UTF_8);
        when(values.setIfAbsent(eq("webhook:nonce:" + nonce), eq("1"),
                eq(Duration.ofMinutes(10)))).thenReturn(true);

        verifier.verify(timestamp, nonce, signature(timestamp, nonce, body), body);

        verify(values).setIfAbsent("webhook:nonce:" + nonce, "1", Duration.ofMinutes(10));
    }

    @Test
    void rejectsReplayedNonce() throws Exception {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = UUID.randomUUID().toString();
        byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
        when(values.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(false);

        assertThatThrownBy(() -> verifier.verify(timestamp, nonce,
                signature(timestamp, nonce, body), body))
                .isInstanceOf(WebhookVerifier.ReplayedWebhookException.class);
        verify(audit).record(null, "WEBHOOK_REPLAYED", "WEBHOOK", null);
    }

    @Test
    void rejectsExpiredOrInvalidSignatureAndAudits() {
        String nonce = UUID.randomUUID().toString();
        assertThatThrownBy(() -> verifier.verify("1", nonce, "sha256=00", new byte[0]))
                .isInstanceOf(WebhookVerifier.InvalidWebhookException.class);
        verify(audit).record(null, "WEBHOOK_REJECTED", "WEBHOOK", null);
    }

    @Test
    void rejectsTamperedBodyBeforeClaimingNonce() throws Exception {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = UUID.randomUUID().toString();
        byte[] original = "{}".getBytes(StandardCharsets.UTF_8);
        byte[] tampered = "{\"x\":1}".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> verifier.verify(timestamp, nonce,
                signature(timestamp, nonce, original), tampered))
                .isInstanceOf(WebhookVerifier.InvalidWebhookException.class);
        verify(audit).record(null, "WEBHOOK_REJECTED", "WEBHOOK", null);
        org.mockito.Mockito.verify(redis, org.mockito.Mockito.never()).opsForValue();
    }

    private String signature(String timestamp, String nonce, byte[] body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        mac.update((timestamp + "\n" + nonce + "\n").getBytes(StandardCharsets.UTF_8));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(body));
    }
}
