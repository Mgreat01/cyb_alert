package com.cybAlert.cybAlert.infrastructure.event;

import com.cybAlert.cybAlert.business.event.SecurityEvent;
import com.cybAlert.cybAlert.business.event.SecurityEventRepository;
import com.cybAlert.cybAlert.business.detection.DetectionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventConsumer {

    private final SecurityEventRepository events;
    private final DetectionService detection;

    public SecurityEventConsumer(SecurityEventRepository events, DetectionService detection) {
        this.events = events;
        this.detection = detection;
    }

    @RetryableTopic(attempts = "4")
    @KafkaListener(topics = "security.events", groupId = "cyberwatch-detection")
    public void consume(String eventId) {
        SecurityEvent event = events.findById(eventId)
                .orElseThrow(() -> new IllegalStateException("Événement absent : " + eventId));
        detection.process(event);
    }
}
