package com.cybAlert.cybAlert.business.alert;

import com.cybAlert.cybAlert.business.audit.AuditService;
import com.cybAlert.cybAlert.infrastructure.alert.SpringDataAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AlertService {

    private final SpringDataAlertRepository alerts;
    private final AuditService audit;

    public AlertService(SpringDataAlertRepository alerts, AuditService audit) {
        this.alerts = alerts;
        this.audit = audit;
    }

    @Transactional
    public AlertEntity changeStatus(UUID id, AlertEntity.Status status) {
        AlertEntity alert = alerts.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alerte introuvable : " + id));
        alert.changeStatus(status);
        AlertEntity saved = alerts.save(alert);
        audit.recordCurrentActor("ALERT_STATUS_CHANGED", "ALERT", id.toString());
        return saved;
    }
}
