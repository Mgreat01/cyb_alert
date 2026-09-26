package com.cybAlert.cybAlert.application.audit;

import com.cybAlert.cybAlert.business.audit.AuditLogEntity;
import com.cybAlert.cybAlert.infrastructure.audit.SpringDataAuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final SpringDataAuditLogRepository logs;

    public AuditController(SpringDataAuditLogRepository logs) { this.logs = logs; }

    @GetMapping
    public Page<AuditLogEntity> findAll(Pageable pageable) { return logs.findAll(pageable); }
}
