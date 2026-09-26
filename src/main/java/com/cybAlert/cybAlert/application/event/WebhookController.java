package com.cybAlert.cybAlert.application.event;

import com.cybAlert.cybAlert.business.audit.AuditService;
import com.cybAlert.cybAlert.business.event.SecurityEvent;
import com.cybAlert.cybAlert.business.event.SecurityEventService;
import com.cybAlert.cybAlert.business.webhook.WebhookVerifier;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final WebhookVerifier verifier;
    private final SecurityEventService events;
    private final AuditService audit;
    private final JsonMapper mapper;
    private final Validator validator;

    public WebhookController(WebhookVerifier verifier, SecurityEventService events,
                             AuditService audit, JsonMapper mapper, Validator validator) {
        this.verifier = verifier;
        this.events = events;
        this.audit = audit;
        this.mapper = mapper;
        this.validator = validator;
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    WebhookResponse accept(@RequestHeader("X-Cyberwatch-Timestamp") String timestamp,
                           @RequestHeader("X-Cyberwatch-Nonce") String nonce,
                           @RequestHeader("X-Cyberwatch-Signature") String signature,
                           @RequestBody byte[] body) {
        verifier.verify(timestamp, nonce, signature, body);
        WebhookEvent request;
        try {
            request = mapper.readValue(body, WebhookEvent.class);
        } catch (RuntimeException exception) {
            throw new InvalidWebhookPayloadException();
        }
        if (request == null || !validator.validate(request).isEmpty()) {
            throw new InvalidWebhookPayloadException();
        }
        SecurityEvent event = events.ingest(request.eventId(), request.eventType(),
                request.timestamp(), request.sourceId(), request.sourceIp(),
                request.destinationIp(), request.sourcePort(), request.destinationPort(),
                request.protocol(), request.username(), request.severity(), request.message(),
                request.metadata());
        audit.record(null, "WEBHOOK_ACCEPTED", "EVENT", event.getEventId());
        return new WebhookResponse(event.getEventId());
    }

    public record WebhookEvent(
            @NotBlank @Size(max = 100) String eventId,
            @NotBlank @Size(max = 100) String eventType,
            @NotNull @PastOrPresent Instant timestamp,
            @NotNull UUID sourceId,
            @Size(max = 45) String sourceIp,
            @Size(max = 45) String destinationIp,
            @Min(0) @Max(65535) Integer sourcePort,
            @Min(0) @Max(65535) Integer destinationPort,
            @Size(max = 20) String protocol,
            @Size(max = 100) String username,
            @NotBlank @Size(max = 20) String severity,
            @NotBlank @Size(max = 4000) String message,
            Map<String, Object> metadata) { }

    public record WebhookResponse(String eventId) { }
    public static class InvalidWebhookPayloadException extends RuntimeException { }
}
