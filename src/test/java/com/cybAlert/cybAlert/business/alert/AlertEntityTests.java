package com.cybAlert.cybAlert.business.alert;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertEntityTests {

    @Test
    void tracksAcknowledgementAndResolutionWithoutBackwardTransition() {
        AlertEntity alert = alert(75);

        alert.changeStatus(AlertEntity.Status.ACKNOWLEDGED);
        assertThat(alert.getAcknowledgedAt()).isNotNull();
        alert.changeStatus(AlertEntity.Status.RESOLVED);
        assertThat(alert.getResolvedAt()).isNotNull();
        assertThatThrownBy(() -> alert.changeStatus(AlertEntity.Status.DETECTED))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boundsRiskScore() {
        assertThat(alert(150).getRiskScore()).isEqualTo(100);
        assertThat(alert(-5).getRiskScore()).isZero();
    }

    private AlertEntity alert(int score) {
        return new AlertEntity(UUID.randomUUID(), UUID.randomUUID(), "Intrusion",
                "Tentative détectée", AlertEntity.Severity.HIGH, score, Instant.now());
    }
}
