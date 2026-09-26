package com.cybAlert.cybAlert.application.dashboard;

import com.cybAlert.cybAlert.business.alert.AlertEntity;
import com.cybAlert.cybAlert.business.incident.IncidentEntity;
import com.cybAlert.cybAlert.business.risk.RiskService;
import com.cybAlert.cybAlert.business.source.SourceEntity;
import com.cybAlert.cybAlert.infrastructure.alert.SpringDataAlertRepository;
import com.cybAlert.cybAlert.infrastructure.event.ElasticsearchSecurityEventRepository;
import com.cybAlert.cybAlert.infrastructure.incident.SpringDataIncidentRepository;
import com.cybAlert.cybAlert.infrastructure.source.SpringDataSourceRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ElasticsearchSecurityEventRepository events;
    private final SpringDataAlertRepository alerts;
    private final SpringDataIncidentRepository incidents;
    private final SpringDataSourceRepository sources;
    private final RiskService risks;

    public DashboardController(ElasticsearchSecurityEventRepository events,
                               SpringDataAlertRepository alerts,
                               SpringDataIncidentRepository incidents,
                               SpringDataSourceRepository sources, RiskService risks) {
        this.events = events;
        this.alerts = alerts;
        this.incidents = incidents;
        this.sources = sources;
        this.risks = risks;
    }

    @GetMapping("/summary")
    public Summary summary() {
        return new Summary(events.count(),
                alerts.countByStatusNot(AlertEntity.Status.RESOLVED),
                alerts.countBySeverityAndStatusNot(AlertEntity.Severity.CRITICAL,
                        AlertEntity.Status.RESOLVED),
                incidents.countByStatusNot(IncidentEntity.Status.CLOSED),
                sources.countByStatus(SourceEntity.Status.OFFLINE),
                risks.highestRisk(5));
    }

    @GetMapping("/priority-alerts")
    public List<PriorityAlert> priorityAlerts() {
        return alerts.findByStatusNotOrderByRiskScoreDescDetectedAtDesc(
                AlertEntity.Status.RESOLVED, PageRequest.of(0, 10)).stream()
                .map(alert -> new PriorityAlert(alert.getId(), alert.getSourceId(),
                        alert.getTitle(), alert.getSeverity(), alert.getRiskScore(),
                        alert.getStatus(), alert.getDetectedAt()))
                .toList();
    }

    @GetMapping("/priority-incidents")
    public List<PriorityIncident> priorityIncidents() {
        return incidents.findPriorityIncidents().stream()
                .map(incident -> new PriorityIncident(incident.getId(), incident.getTitle(),
                        incident.getPriority(), incident.getStatus(),
                        incident.getAssignedTo(), incident.getCreatedAt()))
                .toList();
    }

    record Summary(long events, long openAlerts, long criticalAlerts,
                   long openIncidents, long offlineSources,
                   List<RiskService.SourceRisk> topRiskySources) {
    }

    record PriorityAlert(UUID id, UUID sourceId, String title, AlertEntity.Severity severity,
                         int riskScore, AlertEntity.Status status, Instant detectedAt) { }

    record PriorityIncident(UUID id, String title, IncidentEntity.Priority priority,
                            IncidentEntity.Status status, UUID assignedTo,
                            Instant createdAt) { }
}
