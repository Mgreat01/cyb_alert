package com.cybAlert.cybAlert.infrastructure.event;

import com.cybAlert.cybAlert.business.event.EventOutboxEntity;
import com.cybAlert.cybAlert.business.event.EventPublisher;
import com.cybAlert.cybAlert.business.event.SecurityEvent;
import com.cybAlert.cybAlert.business.event.SecurityEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventOutboxDispatcher {

    private final EventOutboxRepository outbox;
    private final EventPayloadCodec codec;
    private final SecurityEventRepository events;
    private final EventPublisher publisher;

    public EventOutboxDispatcher(EventOutboxRepository outbox, EventPayloadCodec codec,
                                 SecurityEventRepository events, EventPublisher publisher) {
        this.outbox = outbox;
        this.codec = codec;
        this.events = events;
        this.publisher = publisher;
    }

    @Transactional
    public void dispatch(String eventId) {
        EventOutboxEntity pending = outbox.findLockedByEventId(eventId)
                .orElseThrow(() -> new IllegalStateException("Événement absent : " + eventId));
        if (pending.getPublishedAt() != null) { return; }
        SecurityEvent event = codec.decode(pending.getPayload());
        events.save(event);
        publisher.publish(event);
        pending.markPublished();
    }
}
