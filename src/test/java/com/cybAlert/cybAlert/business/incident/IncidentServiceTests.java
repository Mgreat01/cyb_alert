package com.cybAlert.cybAlert.business.incident;

import com.cybAlert.cybAlert.business.alert.AlertEntity;
import com.cybAlert.cybAlert.business.audit.AuditService;
import com.cybAlert.cybAlert.business.user.UserRepository;
import com.cybAlert.cybAlert.infrastructure.alert.SpringDataAlertRepository;
import com.cybAlert.cybAlert.infrastructure.incident.SpringDataIncidentCommentRepository;
import com.cybAlert.cybAlert.infrastructure.incident.SpringDataIncidentHistoryRepository;
import com.cybAlert.cybAlert.infrastructure.incident.SpringDataIncidentRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IncidentServiceTests {

    @Test
    void createsIncidentWithHistoryAndAudit() {
        SpringDataIncidentRepository incidents = mock(SpringDataIncidentRepository.class);
        SpringDataAlertRepository alerts = mock(SpringDataAlertRepository.class);
        SpringDataIncidentHistoryRepository history = mock(SpringDataIncidentHistoryRepository.class);
        AuditService audit = mock(AuditService.class);
        IncidentService service = service(incidents, alerts, history, audit);
        UUID actorId = UUID.randomUUID();
        UUID alertId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        when(alerts.findAllById(Set.of(alertId))).thenReturn(List.of(mock(AlertEntity.class)));
        IncidentEntity saved = mock(IncidentEntity.class);
        when(saved.getId()).thenReturn(incidentId);
        when(incidents.save(any(IncidentEntity.class))).thenReturn(saved);

        service.create(actorId, "Intrusion", "À analyser", AlertEntity.Severity.HIGH,
                IncidentEntity.Priority.HIGH, Set.of(alertId));

        verify(history).save(any(IncidentHistoryEntity.class));
        verify(audit).record(actorId, "INCIDENT_CREATED", "INCIDENT", incidentId.toString());
    }

    @Test
    void rejectsMissingAlertWithoutCreatingIncident() {
        SpringDataIncidentRepository incidents = mock(SpringDataIncidentRepository.class);
        SpringDataAlertRepository alerts = mock(SpringDataAlertRepository.class);
        SpringDataIncidentHistoryRepository history = mock(SpringDataIncidentHistoryRepository.class);
        AuditService audit = mock(AuditService.class);
        IncidentService service = service(incidents, alerts, history, audit);

        assertThatThrownBy(() -> service.create(UUID.randomUUID(), "Intrusion", "À analyser",
                AlertEntity.Severity.HIGH, IncidentEntity.Priority.HIGH,
                Set.of(UUID.randomUUID()))).isInstanceOf(IllegalArgumentException.class);
        verify(incidents, never()).save(any());
        verify(audit, never()).record(any(), any(), any(), any());
    }

    private IncidentService service(SpringDataIncidentRepository incidents,
                                    SpringDataAlertRepository alerts,
                                    SpringDataIncidentHistoryRepository history,
                                    AuditService audit) {
        return new IncidentService(incidents, alerts,
                mock(SpringDataIncidentCommentRepository.class), history,
                mock(UserRepository.class), audit);
    }
}
