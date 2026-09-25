package com.cybAlert.cybAlert.application.incident;

import com.cybAlert.cybAlert.business.alert.AlertEntity;
import com.cybAlert.cybAlert.business.incident.IncidentCommentEntity;
import com.cybAlert.cybAlert.business.incident.IncidentEntity;
import com.cybAlert.cybAlert.business.incident.IncidentHistoryEntity;
import com.cybAlert.cybAlert.business.incident.IncidentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService service;

    public IncidentController(IncidentService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentEntity create(@AuthenticationPrincipal Jwt principal,
                                 @Valid @RequestBody CreateRequest request) {
        return service.create(UUID.fromString(principal.getSubject()), request.title(),
                request.description(), request.severity(), request.priority(),
                request.alertIds());
    }

    @GetMapping
    public Page<IncidentEntity> findAll(@RequestParam(required = false) IncidentEntity.Status status,
                                        Pageable pageable) {
        return service.findAll(status, pageable);
    }

    @GetMapping("/{id}")
    public IncidentEntity findById(@PathVariable UUID id) { return service.findById(id); }

    @PatchMapping("/{id}/status")
    public IncidentEntity changeStatus(@PathVariable UUID id, @AuthenticationPrincipal Jwt principal,
                                       @Valid @RequestBody StatusRequest request) {
        return service.changeStatus(id, UUID.fromString(principal.getSubject()), request.status());
    }

    @PatchMapping("/{id}/assignee")
    public IncidentEntity assign(@PathVariable UUID id, @AuthenticationPrincipal Jwt principal,
                                 @Valid @RequestBody AssignRequest request) {
        return service.assign(id, UUID.fromString(principal.getSubject()), request.userId());
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentCommentEntity comment(@PathVariable UUID id,
                                         @AuthenticationPrincipal Jwt principal,
                                         @Valid @RequestBody CommentRequest request) {
        return service.comment(id, UUID.fromString(principal.getSubject()), request.content());
    }

    @GetMapping("/{id}/comments")
    public List<IncidentCommentEntity> comments(@PathVariable UUID id) {
        return service.comments(id);
    }

    @GetMapping("/{id}/history")
    public List<IncidentHistoryEntity> history(@PathVariable UUID id) {
        return service.history(id);
    }

    record CreateRequest(@NotBlank @Size(max = 200) String title,
                         @NotBlank @Size(max = 4000) String description,
                         @NotNull AlertEntity.Severity severity,
                         @NotNull IncidentEntity.Priority priority,
                         @NotEmpty Set<UUID> alertIds) {
    }

    record StatusRequest(@NotNull IncidentEntity.Status status) {
    }

    record AssignRequest(@NotNull UUID userId) {
    }

    record CommentRequest(@NotBlank @Size(max = 4000) String content) {
    }
}
