package com.cybAlert.cybAlert.infrastructure.event;

import com.cybAlert.cybAlert.business.event.SecurityEvent;
import com.cybAlert.cybAlert.business.event.SecurityEventRepository;
import com.cybAlert.cybAlert.business.detection.DetectionService;
import com.cybAlert.cybAlert.business.risk.RiskService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventConsumer {

    private final SecurityEventRepository events;
    private final DetectionService detection;
    private final RiskService risk;

    public SecurityEventConsumer(SecurityEventRepository events, DetectionService detection,
                                 RiskService risk) {
        this.events = events;
        this.detection = detection;
        this.risk = risk;
    }

    @RetryableTopic(attempts = "4")
    @KafkaListener(topics = "security.events", groupId = "cyberwatch-detection")
    public void consume(String eventId) {
        SecurityEvent event = events.findById(eventId)
                .orElseThrow(() -> new IllegalStateException("Événement absent : " + eventId));
        detection.process(event);
        risk.apply(event);
    }
}
