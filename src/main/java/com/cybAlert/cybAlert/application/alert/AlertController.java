package com.cybAlert.cybAlert.application.alert;

import com.cybAlert.cybAlert.business.alert.AlertEntity;
import com.cybAlert.cybAlert.infrastructure.alert.SpringDataAlertRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final SpringDataAlertRepository alerts;

    public AlertController(SpringDataAlertRepository alerts) { this.alerts = alerts; }

    @GetMapping
    public Page<AlertEntity> findAll(@RequestParam(required = false) AlertEntity.Status status,
                                     @RequestParam(required = false) AlertEntity.Severity severity,
                                     Pageable pageable) {
        if (status != null) { return alerts.findByStatus(status, pageable); }
        if (severity != null) { return alerts.findBySeverity(severity, pageable); }
        return alerts.findAll(pageable);
    }

    @GetMapping("/{id}")
    public AlertEntity findById(@PathVariable UUID id) {
        return alerts.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alerte introuvable : " + id));
    }

    @PatchMapping("/{id}")
    public AlertEntity changeStatus(@PathVariable UUID id,
                                    @Valid @RequestBody StatusRequest request) {
        AlertEntity alert = findById(id);
        alert.changeStatus(request.status());
        return alerts.save(alert);
    }

    record StatusRequest(@NotNull AlertEntity.Status status) {
    }
}
