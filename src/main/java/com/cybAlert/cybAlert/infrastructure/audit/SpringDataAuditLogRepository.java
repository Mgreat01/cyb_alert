package com.cybAlert.cybAlert.infrastructure.audit;

import com.cybAlert.cybAlert.business.audit.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataAuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {
}
