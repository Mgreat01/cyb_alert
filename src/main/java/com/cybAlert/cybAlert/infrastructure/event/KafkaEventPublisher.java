package com.cybAlert.cybAlert.infrastructure.event;

import com.cybAlert.cybAlert.business.event.EventPublisher;
import com.cybAlert.cybAlert.business.event.SecurityEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafka;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafka) {
        this.kafka = kafka;
    }

    @Override
    public void publish(SecurityEvent event) {
        try {
            kafka.send("security.events", event.getSourceId().toString(), event.getEventId()).get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publication Kafka interrompue", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Publication Kafka impossible", exception);
        }
    }
}
