package com.cybAlert.cybAlert.application.dashboard;

import com.cybAlert.cybAlert.business.alert.AlertEntity;
import com.cybAlert.cybAlert.business.incident.IncidentEntity;
import com.cybAlert.cybAlert.business.risk.RiskService;
import com.cybAlert.cybAlert.business.source.SourceEntity;
import com.cybAlert.cybAlert.infrastructure.alert.SpringDataAlertRepository;
import com.cybAlert.cybAlert.infrastructure.event.ElasticsearchSecurityEventRepository;
import com.cybAlert.cybAlert.infrastructure.incident.SpringDataIncidentRepository;
import com.cybAlert.cybAlert.infrastructure.source.SpringDataSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardControllerTests {

    @Test
    void buildsSummaryAndPriorityViews() {
        ElasticsearchSecurityEventRepository events = mock(ElasticsearchSecurityEventRepository.class);
        SpringDataAlertRepository alerts = mock(SpringDataAlertRepository.class);
        SpringDataIncidentRepository incidents = mock(SpringDataIncidentRepository.class);
        SpringDataSourceRepository sources = mock(SpringDataSourceRepository.class);
        RiskService risks = mock(RiskService.class);
        DashboardController dashboard = new DashboardController(events, alerts, incidents,
                sources, risks);
        UUID sourceId = UUID.randomUUID();
        AlertEntity alert = new AlertEntity(UUID.randomUUID(), sourceId, "Intrusion",
                "Tentative détectée", AlertEntity.Severity.CRITICAL, 95, Instant.now());
        IncidentEntity incident = new IncidentEntity(UUID.randomUUID(), "Intrusion",
                "À analyser", AlertEntity.Severity.CRITICAL,
                IncidentEntity.Priority.CRITICAL, Set.of(alert));
        when(events.count()).thenReturn(120L);
        when(alerts.countByStatusNot(AlertEntity.Status.RESOLVED)).thenReturn(3L);
        when(alerts.countBySeverityAndStatusNot(AlertEntity.Severity.CRITICAL,
                AlertEntity.Status.RESOLVED)).thenReturn(1L);
        when(incidents.countByStatusNot(IncidentEntity.Status.CLOSED)).thenReturn(2L);
        when(sources.countByStatus(SourceEntity.Status.OFFLINE)).thenReturn(1L);
        when(risks.highestRisk(5)).thenReturn(List.of(new RiskService.SourceRisk(sourceId, 95)));
        when(alerts.findByStatusNotOrderByRiskScoreDescDetectedAtDesc(
                AlertEntity.Status.RESOLVED, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(alert)));
        when(incidents.findPriorityIncidents()).thenReturn(List.of(incident));

        assertThat(dashboard.summary().events()).isEqualTo(120);
        assertThat(dashboard.summary().topRiskySources()).hasSize(1);
        assertThat(dashboard.priorityAlerts()).singleElement()
                .extracting(DashboardController.PriorityAlert::riskScore).isEqualTo(95);
        assertThat(dashboard.priorityIncidents()).singleElement()
                .extracting(DashboardController.PriorityIncident::priority)
                .isEqualTo(IncidentEntity.Priority.CRITICAL);
    }
}
