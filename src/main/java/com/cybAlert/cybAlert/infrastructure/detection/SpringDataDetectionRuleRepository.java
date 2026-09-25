package com.cybAlert.cybAlert.infrastructure.detection;

import com.cybAlert.cybAlert.business.detection.DetectionRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataDetectionRuleRepository extends JpaRepository<DetectionRuleEntity, UUID> {

    List<DetectionRuleEntity> findByEventTypeAndEnabledTrue(String eventType);
}
