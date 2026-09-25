package com.cybAlert.cybAlert.business.incident;

import com.cybAlert.cybAlert.business.alert.AlertEntity;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IncidentEntityTests {

    @Test
    void tracksAssignmentAndResolution() {
        AlertEntity alert = new AlertEntity(UUID.randomUUID(), UUID.randomUUID(),
                "Brute force", "Multiple échecs", AlertEntity.Severity.HIGH, 75,
                java.time.Instant.now());
        IncidentEntity incident = new IncidentEntity(UUID.randomUUID(), "Intrusion",
                "Analyse requise", AlertEntity.Severity.HIGH,
                IncidentEntity.Priority.HIGH, Set.of(alert));
        UUID assignee = UUID.randomUUID();

        incident.assign(assignee);
        incident.changeStatus(IncidentEntity.Status.RESOLVED);

        assertThat(incident.getAssignedTo()).isEqualTo(assignee);
        assertThat(incident.getResolvedAt()).isNotNull();
        assertThat(incident.getAlerts()).containsExactly(alert);
    }
}
