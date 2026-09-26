package com.cybAlert.cybAlert.infrastructure.event;

import com.cybAlert.cybAlert.business.event.SecurityEvent;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventPayloadCodecTests {

    @Test
    void preservesEventFieldsThroughOutboxPayload() {
        EventPayloadCodec codec = new EventPayloadCodec(JsonMapper.builder().build());
        UUID sourceId = UUID.randomUUID();
        SecurityEvent original = new SecurityEvent("evt-1", "LOGIN_FAILED", Instant.now(),
                sourceId, "10.0.0.1", "10.0.0.2", 12345, 443, "TCP", "alice", "HIGH",
                "Connexion refusée", Map.of("agent", "linux"));

        SecurityEvent restored = codec.decode(codec.encode(original));

        assertThat(restored.getEventId()).isEqualTo(original.getEventId());
        assertThat(restored.getTimestamp()).isEqualTo(original.getTimestamp());
        assertThat(restored.getSourceId()).isEqualTo(sourceId);
        assertThat(restored.getMetadata()).containsEntry("agent", "linux");
    }
}
