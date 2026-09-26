package com.cybAlert.cybAlert.infrastructure.event;

import com.cybAlert.cybAlert.business.event.EventOutboxEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class EventOutboxWorker {

    private static final Logger LOG = LoggerFactory.getLogger(EventOutboxWorker.class);
    private final EventOutboxRepository outbox;
    private final EventOutboxDispatcher dispatcher;

    public EventOutboxWorker(EventOutboxRepository outbox, EventOutboxDispatcher dispatcher) {
        this.outbox = outbox;
        this.dispatcher = dispatcher;
    }

    @Scheduled(fixedDelayString = "${cyberwatch.outbox.poll-delay:5000}")
    public void publishPending() {
        for (EventOutboxEntity pending : outbox.findByPublishedAtIsNullOrderByCreatedAtAsc(
                PageRequest.of(0, 50))) {
            try {
                dispatcher.dispatch(pending.getEventId());
            } catch (RuntimeException exception) {
                LOG.error("Publication impossible pour l'événement {}", pending.getEventId(),
                        exception);
            }
        }
    }
}
