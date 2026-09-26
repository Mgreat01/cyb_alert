package com.cybAlert.cybAlert.business.event;

import com.cybAlert.cybAlert.business.source.SourceRepository;
import com.cybAlert.cybAlert.infrastructure.event.EventOutboxRepository;
import com.cybAlert.cybAlert.infrastructure.event.EventPayloadCodec;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class SecurityEventService {

    private final SourceRepository sources;
    private final EventOutboxRepository outbox;
    private final EventPayloadCodec codec;

    public SecurityEventService(SourceRepository sources,
                                EventOutboxRepository outbox, EventPayloadCodec codec) {
        this.sources = sources;
        this.outbox = outbox;
        this.codec = codec;
    }

    @Transactional
    public SecurityEvent ingest(String eventId, String eventType, Instant timestamp,
                                UUID sourceId, String sourceIp, String destinationIp,
                                Integer sourcePort, Integer destinationPort, String protocol,
                                String username, String severity, String message,
                                Map<String, Object> metadata) {
        String normalizedId = eventId.strip();
        if (outbox.existsById(normalizedId)) {
            throw new DuplicateEventException(normalizedId);
        }
        if (sources.findById(sourceId).isEmpty()) {
            throw new UnknownEventSourceException(sourceId);
        }
        SecurityEvent event = new SecurityEvent(normalizedId, uppercase(eventType), timestamp,
                sourceId, strip(sourceIp), strip(destinationIp), sourcePort, destinationPort,
                uppercase(protocol), strip(username), uppercase(severity), message.strip(),
                metadata == null ? Map.of() : Map.copyOf(metadata));
        outbox.saveAndFlush(new EventOutboxEntity(normalizedId, codec.encode(event)));
        return event;
    }

    private String uppercase(String value) {
        return value == null ? null : value.strip().toUpperCase(Locale.ROOT);
    }

    private String strip(String value) {
        return value == null ? null : value.strip();
    }

    public static class DuplicateEventException extends IllegalArgumentException {
        public DuplicateEventException(String id) { super("Événement déjà ingéré : " + id); }
    }

    public static class UnknownEventSourceException extends IllegalArgumentException {
        public UnknownEventSourceException(UUID id) { super("Source inconnue : " + id); }
    }
}
