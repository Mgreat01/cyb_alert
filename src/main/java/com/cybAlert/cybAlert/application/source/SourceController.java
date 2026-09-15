package com.cybAlert.cybAlert.application.source;

import com.cybAlert.cybAlert.business.source.SourceEntity;
import com.cybAlert.cybAlert.business.source.SourceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/sources")
public class SourceController {

    private final SourceService service;

    public SourceController(SourceService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    SourceResponse create(@Valid @RequestBody SourceRequest request) {
        return SourceResponse.from(service.create(request.hostname(), request.ipAddress(),
                request.macAddress(), request.operatingSystem(), request.type(),
                request.environment()));
    }

    @GetMapping
    Page<SourceResponse> findAll(Pageable pageable) {
        return service.findAll(pageable).map(SourceResponse::from);
    }

    @GetMapping("/{id}")
    SourceResponse findById(@PathVariable UUID id) {
        return SourceResponse.from(service.findById(id));
    }

    @PutMapping("/{id}")
    SourceResponse update(@PathVariable UUID id, @Valid @RequestBody SourceRequest request) {
        return SourceResponse.from(service.update(id, request.hostname(), request.ipAddress(),
                request.macAddress(), request.operatingSystem(), request.type(),
                request.environment()));
    }

    @PatchMapping("/{id}/heartbeat")
    SourceResponse heartbeat(@PathVariable UUID id) {
        return SourceResponse.from(service.heartbeat(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id) { service.delete(id); }

    record SourceRequest(
            @NotBlank @Size(max = 255) String hostname,
            @NotBlank @Pattern(regexp = "^[0-9a-fA-F:.]+$") String ipAddress,
            @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}$") String macAddress,
            @Size(max = 100) String operatingSystem,
            @NotNull SourceEntity.Type type,
            @NotBlank @Size(max = 50) String environment) {
    }

    record SourceResponse(UUID id, String hostname, String ipAddress, String macAddress,
                          String operatingSystem, SourceEntity.Type type, String environment,
                          SourceEntity.Status status, Instant lastSeenAt, Instant createdAt) {
        static SourceResponse from(SourceEntity source) {
            return new SourceResponse(source.getId(), source.getHostname(), source.getIpAddress(),
                    source.getMacAddress(), source.getOperatingSystem(), source.getType(),
                    source.getEnvironment(), source.getStatus(), source.getLastSeenAt(),
                    source.getCreatedAt());
        }
    }
}
