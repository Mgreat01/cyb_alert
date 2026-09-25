package com.cybAlert.cybAlert.business.detection;

import com.cybAlert.cybAlert.business.alert.AlertEntity;
import com.cybAlert.cybAlert.business.event.SecurityEvent;
import com.cybAlert.cybAlert.infrastructure.alert.SpringDataAlertRepository;
import com.cybAlert.cybAlert.infrastructure.detection.SpringDataDetectionObservationRepository;
import com.cybAlert.cybAlert.infrastructure.detection.SpringDataDetectionRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class DetectionService {

    private final SpringDataDetectionObservationRepository observations;
    private final SpringDataDetectionRuleRepository rules;
    private final SpringDataAlertRepository alerts;

    public DetectionService(SpringDataDetectionObservationRepository observations,
                            SpringDataDetectionRuleRepository rules,
                            SpringDataAlertRepository alerts) {
        this.observations = observations;
        this.rules = rules;
        this.alerts = alerts;
    }

    @Transactional
    public void process(SecurityEvent event) {
        if (observations.existsById(event.getEventId())) {
            return;
        }
        observations.saveAndFlush(new DetectionObservationEntity(event.getEventId(),
                event.getEventType(), event.getSourceIp(), event.getDestinationIp(),
                event.getTimestamp()));
        if (event.getSourceIp() == null) {
            return;
        }
        for (DetectionRuleEntity rule : rules.findByEventTypeAndEnabledTrue(event.getEventType())) {
            long count = countMatches(rule, event);
            if (count >= rule.getThresholdCount()
                    && count % rule.getThresholdCount() == 0) {
                AlertEntity alert = new AlertEntity(rule.getId(), event.getSourceId(),
                        rule.getName(), "Détection de " + rule.getName() + " depuis "
                        + event.getSourceIp(), AlertEntity.Severity.valueOf(rule.getSeverity()),
                        severityScore(rule.getSeverity()), Instant.now());
                alerts.save(alert);
            }
        }
    }

    private long countMatches(DetectionRuleEntity rule, SecurityEvent event) {
        Instant start = event.getTimestamp().minusSeconds(rule.getWindowSeconds());
        if ("PORT_SCAN".equals(rule.getEventType())) {
            return observations.countDistinctDestinations(rule.getEventType(),
                    event.getSourceIp(), start, event.getTimestamp());
        }
        return observations.countByEventTypeAndSourceIpAndEventTimeBetween(
                rule.getEventType(), event.getSourceIp(), start, event.getTimestamp());
    }

    private int severityScore(String severity) {
        return switch (severity) {
            case "LOW" -> 20;
            case "MEDIUM" -> 45;
            case "HIGH" -> 75;
            case "CRITICAL" -> 100;
            default -> throw new IllegalArgumentException("Sévérité inconnue : " + severity);
        };
    }
}
