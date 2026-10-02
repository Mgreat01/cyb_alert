package com.cybAlert.cybAlert.application.alert;

import com.cybAlert.cybAlert.business.alert.AlertEntity;
import com.cybAlert.cybAlert.business.alert.AlertService;
import com.cybAlert.cybAlert.infrastructure.alert.SpringDataAlertRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertControllerTests {

    @Test
    void combinesStatusAndSeverityFilters() {
        SpringDataAlertRepository alerts = mock(SpringDataAlertRepository.class);
        Pageable pageable = Pageable.unpaged();
        when(alerts.findByStatusAndSeverity(AlertEntity.Status.DETECTED,
                AlertEntity.Severity.CRITICAL, pageable)).thenReturn(Page.empty());

        Page<AlertEntity> result = new AlertController(alerts, mock(AlertService.class)).findAll(
                AlertEntity.Status.DETECTED, AlertEntity.Severity.CRITICAL, pageable);

        assertThat(result).isEmpty();
        verify(alerts).findByStatusAndSeverity(AlertEntity.Status.DETECTED,
                AlertEntity.Severity.CRITICAL, pageable);
    }
}
