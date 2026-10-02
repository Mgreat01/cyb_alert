package com.cybAlert.cybAlert.application.detection;

import com.cybAlert.cybAlert.business.detection.DetectionRuleEntity;
import com.cybAlert.cybAlert.business.detection.DetectionRuleService;
import com.cybAlert.cybAlert.infrastructure.detection.SpringDataDetectionRuleRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/detection-rules")
public class DetectionRuleController {

    private final SpringDataDetectionRuleRepository rules;
    private final DetectionRuleService service;

    public DetectionRuleController(SpringDataDetectionRuleRepository rules,
                                   DetectionRuleService service) {
        this.rules = rules;
        this.service = service;
    }

    @GetMapping
    public List<DetectionRuleEntity> findAll() { return rules.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DetectionRuleEntity create(@Valid @RequestBody RuleRequest request) {
        return service.create(request.name(), request.eventType(),
                request.thresholdCount(), request.windowSeconds(), request.severity(),
                request.enabled());
    }

    @PutMapping("/{id}")
    public DetectionRuleEntity update(@PathVariable UUID id,
                                      @Valid @RequestBody RuleRequest request) {
        return service.update(id, request.name(), request.eventType(),
                request.thresholdCount(), request.windowSeconds(), request.severity(),
                request.enabled());
    }

    record RuleRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 100) String eventType,
            @Min(1) @Max(1000) int thresholdCount,
            @Min(1) @Max(86400) int windowSeconds,
            @NotNull @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL") String severity,
            boolean enabled) {
    }
}
