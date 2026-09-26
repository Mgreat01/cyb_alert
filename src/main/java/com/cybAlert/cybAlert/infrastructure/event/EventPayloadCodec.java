package com.cybAlert.cybAlert.infrastructure.event;

import com.cybAlert.cybAlert.business.event.SecurityEvent;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class EventPayloadCodec {

    private final JsonMapper mapper;

    public EventPayloadCodec(JsonMapper mapper) { this.mapper = mapper; }

    public String encode(SecurityEvent event) {
        return mapper.writeValueAsString(new Payload(event.getEventId(), event.getEventType(),
                event.getTimestamp(), event.getSourceId(), event.getSourceIp(),
                event.getDestinationIp(), event.getSourcePort(), event.getDestinationPort(),
                event.getProtocol(), event.getUsername(), event.getSeverity(),
                event.getMessage(), event.getMetadata()));
    }

    public SecurityEvent decode(String payload) {
        Payload decoded = mapper.readValue(payload, Payload.class);
        return new SecurityEvent(decoded.eventId(), decoded.eventType(), decoded.timestamp(),
                decoded.sourceId(), decoded.sourceIp(), decoded.destinationIp(),
                decoded.sourcePort(), decoded.destinationPort(), decoded.protocol(),
                decoded.username(), decoded.severity(), decoded.message(), decoded.metadata());
    }

    private record Payload(String eventId, String eventType, Instant timestamp, UUID sourceId,
                           String sourceIp, String destinationIp, Integer sourcePort,
                           Integer destinationPort, String protocol, String username,
                           String severity, String message, Map<String, Object> metadata) { }
}
