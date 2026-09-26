package com.cybAlert.cybAlert.infrastructure.event;

import com.cybAlert.cybAlert.business.event.EventOutboxEntity;
import com.cybAlert.cybAlert.business.event.EventPublisher;
import com.cybAlert.cybAlert.business.event.SecurityEvent;
import com.cybAlert.cybAlert.business.event.SecurityEventRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventOutboxDispatcherTests {

    @Test
    void marksPublishedOnlyAfterIndexingAndKafkaAcknowledgement() {
        EventOutboxRepository outbox = mock(EventOutboxRepository.class);
        EventPayloadCodec codec = mock(EventPayloadCodec.class);
        SecurityEventRepository events = mock(SecurityEventRepository.class);
        EventPublisher publisher = mock(EventPublisher.class);
        EventOutboxEntity pending = new EventOutboxEntity("evt-1", "payload");
        SecurityEvent event = event();
        when(outbox.findLockedByEventId("evt-1")).thenReturn(Optional.of(pending));
        when(codec.decode("payload")).thenReturn(event);

        new EventOutboxDispatcher(outbox, codec, events, publisher).dispatch("evt-1");

        verify(events).save(event);
        verify(publisher).publish(event);
        assertThat(pending.getPublishedAt()).isNotNull();
    }

    @Test
    void leavesEventPendingWhenKafkaFails() {
        EventOutboxRepository outbox = mock(EventOutboxRepository.class);
        EventPayloadCodec codec = mock(EventPayloadCodec.class);
        SecurityEventRepository events = mock(SecurityEventRepository.class);
        EventPublisher publisher = mock(EventPublisher.class);
        EventOutboxEntity pending = new EventOutboxEntity("evt-1", "payload");
        SecurityEvent event = event();
        when(outbox.findLockedByEventId("evt-1")).thenReturn(Optional.of(pending));
        when(codec.decode("payload")).thenReturn(event);
        doThrow(new IllegalStateException("Kafka indisponible")).when(publisher).publish(event);

        assertThatThrownBy(() -> new EventOutboxDispatcher(outbox, codec, events,
                publisher).dispatch("evt-1")).isInstanceOf(IllegalStateException.class);
        assertThat(pending.getPublishedAt()).isNull();
    }

    private SecurityEvent event() {
        return new SecurityEvent("evt-1", "LOGIN_FAILED", Instant.now(), UUID.randomUUID(),
                "10.0.0.1", null, null, null, "TCP", null, "HIGH", "Échec", Map.of());
    }
}
