package com.cybAlert.cybAlert.business.event;

import com.cybAlert.cybAlert.business.source.SourceEntity;
import com.cybAlert.cybAlert.business.source.SourceRepository;
import com.cybAlert.cybAlert.infrastructure.event.EventOutboxRepository;
import com.cybAlert.cybAlert.infrastructure.event.EventPayloadCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityEventServiceTests {

    private SourceRepository sources;
    private EventOutboxRepository outbox;
    private EventPayloadCodec codec;
    private SecurityEventService service;

    @BeforeEach
    void setUp() {
        sources = mock(SourceRepository.class);
        outbox = mock(EventOutboxRepository.class);
        codec = mock(EventPayloadCodec.class);
        service = new SecurityEventService(sources, outbox, codec);
    }

    @Test
    void normalizesAndStoresAnEvent() {
        UUID sourceId = UUID.randomUUID();
        when(sources.findById(sourceId)).thenReturn(Optional.of(mock(SourceEntity.class)));

        SecurityEvent event = service.ingest(" evt-42 ", " login_failed ", Instant.now(),
                sourceId, " 10.0.0.1 ", null, 443, null, " tcp ", " alice ",
                " high ", " Invalid login ", Map.of("agent", "linux"));

        assertThat(event.getEventId()).isEqualTo("evt-42");
        assertThat(event.getEventType()).isEqualTo("LOGIN_FAILED");
        assertThat(event.getProtocol()).isEqualTo("TCP");
        assertThat(event.getSeverity()).isEqualTo("HIGH");
        org.mockito.Mockito.verify(outbox).saveAndFlush(any(EventOutboxEntity.class));
    }

    @Test
    void rejectsAnAlreadyIngestedEvent() {
        when(outbox.existsById("evt-42")).thenReturn(true);

        assertThatThrownBy(() -> service.ingest("evt-42", "LOGIN_FAILED", Instant.now(),
                UUID.randomUUID(), null, null, null, null, null, null,
                "HIGH", "Invalid login", Map.of()))
                .isInstanceOf(SecurityEventService.DuplicateEventException.class);
    }
}
