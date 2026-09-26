package com.cybAlert.cybAlert.business.audit;

import com.cybAlert.cybAlert.infrastructure.audit.SpringDataAuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private final SpringDataAuditLogRepository logs;

    public AuditService(SpringDataAuditLogRepository logs) { this.logs = logs; }

    public void record(UUID userId, String action, String resourceType, String resourceId) {
        logs.save(new AuditLogEntity(userId, action, resourceType, resourceId));
    }
}
