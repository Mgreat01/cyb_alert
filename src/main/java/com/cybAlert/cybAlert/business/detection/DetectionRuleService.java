package com.cybAlert.cybAlert.business.detection;

import com.cybAlert.cybAlert.business.audit.AuditService;
import com.cybAlert.cybAlert.infrastructure.detection.SpringDataDetectionRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DetectionRuleService {

    private final SpringDataDetectionRuleRepository rules;
    private final AuditService audit;

    public DetectionRuleService(SpringDataDetectionRuleRepository rules, AuditService audit) {
        this.rules = rules;
        this.audit = audit;
    }

    @Transactional
    public DetectionRuleEntity create(String name, String eventType, int thresholdCount,
                                      int windowSeconds, String severity, boolean enabled) {
        DetectionRuleEntity rule = rules.save(new DetectionRuleEntity(name, eventType,
                thresholdCount, windowSeconds, severity, enabled));
        audit.recordCurrentActor("RULE_CREATED", "DETECTION_RULE",
                String.valueOf(rule.getId()));
        return rule;
    }

    @Transactional
    public DetectionRuleEntity update(UUID id, String name, String eventType,
                                      int thresholdCount, int windowSeconds,
                                      String severity, boolean enabled) {
        DetectionRuleEntity rule = rules.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Règle introuvable : " + id));
        rule.update(name, eventType, thresholdCount, windowSeconds, severity, enabled);
        DetectionRuleEntity saved = rules.save(rule);
        audit.recordCurrentActor("RULE_UPDATED", "DETECTION_RULE", id.toString());
        return saved;
    }
}
