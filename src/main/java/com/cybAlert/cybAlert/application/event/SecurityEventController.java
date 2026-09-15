package com.cybAlert.cybAlert.application.event;

import com.cybAlert.cybAlert.business.event.SecurityEvent;
import com.cybAlert.cybAlert.business.event.SecurityEventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class SecurityEventController {

    private final SecurityEventService service;

    public SecurityEventController(SecurityEventService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EventResponse ingest(@Valid @RequestBody EventRequest request) {
        SecurityEvent event = service.ingest(request.eventId(), request.eventType(),
                request.timestamp(), request.sourceId(), request.sourceIp(),
                request.destinationIp(), request.sourcePort(), request.destinationPort(),
                request.protocol(), request.username(), request.severity(), request.message(),
                request.metadata());
        return new EventResponse(event.getEventId(), event.getTimestamp());
    }

    record EventRequest(
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
            Map<String, Object> metadata) {
    }

    record EventResponse(String eventId, Instant timestamp) {
    }
}
