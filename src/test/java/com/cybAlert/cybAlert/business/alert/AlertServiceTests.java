package com.cybAlert.cybAlert.business.alert;

import com.cybAlert.cybAlert.business.audit.AuditService;
import com.cybAlert.cybAlert.infrastructure.alert.SpringDataAlertRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertServiceTests {

    @Test
    void changesStatusAndAuditsAction() {
        SpringDataAlertRepository alerts = mock(SpringDataAlertRepository.class);
        AuditService audit = mock(AuditService.class);
        UUID id = UUID.randomUUID();
        AlertEntity alert = new AlertEntity(UUID.randomUUID(), UUID.randomUUID(), "Intrusion",
                "Tentative détectée", AlertEntity.Severity.HIGH, 75, Instant.now());
        when(alerts.findById(id)).thenReturn(Optional.of(alert));
        when(alerts.save(alert)).thenReturn(alert);

        AlertEntity result = new AlertService(alerts, audit)
                .changeStatus(id, AlertEntity.Status.ACKNOWLEDGED);

        assertThat(result.getStatus()).isEqualTo(AlertEntity.Status.ACKNOWLEDGED);
        verify(audit).recordCurrentActor("ALERT_STATUS_CHANGED", "ALERT", id.toString());
    }
}
