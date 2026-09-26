package com.cybAlert.cybAlert.business.webhook;

import com.cybAlert.cybAlert.business.audit.AuditService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class WebhookVerifier {

    private static final Duration ALLOWED_SKEW = Duration.ofMinutes(5);
    private static final Duration NONCE_TTL = Duration.ofMinutes(10);
    private final StringRedisTemplate redis;
    private final AuditService audit;
    private final byte[] secret;

    public WebhookVerifier(StringRedisTemplate redis, AuditService audit,
                           @Value("${cyberwatch.webhook.secret:}") String secret) {
        this.redis = redis;
        this.audit = audit;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public void verify(String timestamp, String nonce, String signature, byte[] body) {
        try {
            if (secret.length < 32 || timestamp == null || nonce == null || signature == null
                    || body == null || body.length > 1_048_576) {
                throw new InvalidWebhookException();
            }
            long epochSeconds = Long.parseLong(timestamp);
            UUID.fromString(nonce);
            Instant sentAt = Instant.ofEpochSecond(epochSeconds);
            if (Duration.between(sentAt, Instant.now()).abs().compareTo(ALLOWED_SKEW) > 0) {
                throw new InvalidWebhookException();
            }
            byte[] received = HexFormat.of().parseHex(signature.replaceFirst("^sha256=", ""));
            byte[] expected = sign(timestamp, nonce, body);
            if (!MessageDigest.isEqual(received, expected)) {
                throw new InvalidWebhookException();
            }
            Boolean claimed = redis.opsForValue().setIfAbsent("webhook:nonce:" + nonce,
                    "1", NONCE_TTL);
            if (!Boolean.TRUE.equals(claimed)) { throw new ReplayedWebhookException(); }
        } catch (InvalidWebhookException | IllegalArgumentException | DateTimeException exception) {
            audit.record(null, "WEBHOOK_REJECTED", "WEBHOOK", null);
            throw new InvalidWebhookException();
        } catch (ReplayedWebhookException exception) {
            audit.record(null, "WEBHOOK_REPLAYED", "WEBHOOK", null);
            throw exception;
        }
    }

    private byte[] sign(String timestamp, String nonce, byte[] body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            mac.update(timestamp.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) '\n');
            mac.update(nonce.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) '\n');
            return mac.doFinal(body);
        } catch (Exception exception) {
            throw new IllegalStateException("HMAC-SHA256 indisponible", exception);
        }
    }

    public static class InvalidWebhookException extends RuntimeException { }
    public static class ReplayedWebhookException extends RuntimeException { }
}
