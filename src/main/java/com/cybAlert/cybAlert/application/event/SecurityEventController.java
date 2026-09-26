package com.cybAlert.cybAlert.application.event;

import com.cybAlert.cybAlert.business.event.SecurityEvent;
import com.cybAlert.cybAlert.business.event.SecurityEventService;
import com.cybAlert.cybAlert.infrastructure.event.ElasticsearchSecurityEventRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.Locale;

@RestController
@RequestMapping("/api/events")
public class SecurityEventController {

    private final SecurityEventService service;
    private final ElasticsearchSecurityEventRepository events;

    public SecurityEventController(SecurityEventService service,
                                   ElasticsearchSecurityEventRepository events) {
        this.service = service;
        this.events = events;
    }

    @GetMapping
    Page<SecurityEvent> search(@RequestParam(required = false) String eventType,
                               @RequestParam(required = false) String sourceIp,
                               @RequestParam(required = false) String severity,
                               Pageable pageable) {
        return events.search(normalize(eventType), sourceIp, normalize(severity), pageable);
    }

    @GetMapping("/{id}")
    SecurityEvent findById(@PathVariable String id) {
        return events.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Événement introuvable : " + id));
    }

    private String normalize(String value) {
        return value == null ? null : value.strip().toUpperCase(Locale.ROOT);
    }

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
