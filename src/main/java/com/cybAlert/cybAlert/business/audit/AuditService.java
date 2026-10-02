package com.cybAlert.cybAlert.business.audit;

import com.cybAlert.cybAlert.infrastructure.audit.SpringDataAuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private final SpringDataAuditLogRepository logs;

    public AuditService(SpringDataAuditLogRepository logs) { this.logs = logs; }

    public void record(UUID userId, String action, String resourceType, String resourceId) {
        logs.save(new AuditLogEntity(userId, action, resourceType, resourceId));
    }

    public void recordCurrentActor(String action, String resourceType, String resourceId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID actorId = null;
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            actorId = UUID.fromString(jwt.getSubject());
        }
        record(actorId, action, resourceType, resourceId);
    }
}
