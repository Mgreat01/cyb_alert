package com.cybAlert.cybAlert.business.detection;

import com.cybAlert.cybAlert.business.audit.AuditService;
import com.cybAlert.cybAlert.infrastructure.detection.SpringDataDetectionRuleRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DetectionRuleServiceTests {

    @Test
    void auditsRuleCreationAndUpdate() {
        SpringDataDetectionRuleRepository rules = mock(SpringDataDetectionRuleRepository.class);
        AuditService audit = mock(AuditService.class);
        DetectionRuleService service = new DetectionRuleService(rules, audit);
        when(rules.save(any(DetectionRuleEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        DetectionRuleEntity created = service.create("Brute force", "LOGIN_FAILED", 5,
                60, "HIGH", true);
        assertThat(created.getName()).isEqualTo("Brute force");
        verify(audit).recordCurrentActor("RULE_CREATED", "DETECTION_RULE", "null");

        UUID id = UUID.randomUUID();
        when(rules.findById(id)).thenReturn(Optional.of(created));
        service.update(id, "Brute force", "LOGIN_FAILED", 6, 60, "HIGH", true);

        verify(audit).recordCurrentActor("RULE_UPDATED", "DETECTION_RULE", id.toString());
        assertThat(created.getThresholdCount()).isEqualTo(6);
    }
}
