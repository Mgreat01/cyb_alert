package com.cybAlert.cybAlert.infrastructure.alert;

import com.cybAlert.cybAlert.business.alert.AlertEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataAlertRepository extends JpaRepository<AlertEntity, UUID> {

    Page<AlertEntity> findByStatus(AlertEntity.Status status, Pageable pageable);
    Page<AlertEntity> findBySeverity(AlertEntity.Severity severity, Pageable pageable);
    Page<AlertEntity> findByStatusAndSeverity(AlertEntity.Status status,
                                               AlertEntity.Severity severity, Pageable pageable);
    Page<AlertEntity> findByStatusNotOrderByRiskScoreDescDetectedAtDesc(
            AlertEntity.Status status, Pageable pageable);
    long countByStatusNot(AlertEntity.Status status);
    long countBySeverityAndStatusNot(AlertEntity.Severity severity,
                                     AlertEntity.Status status);
}
