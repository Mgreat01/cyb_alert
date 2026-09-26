package com.cybAlert.cybAlert.infrastructure.event;

import com.cybAlert.cybAlert.business.event.EventOutboxEntity;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventOutboxWorkerTests {

    @Test
    void continuesWithNextEventAfterOnePublicationFails() {
        EventOutboxRepository outbox = mock(EventOutboxRepository.class);
        EventOutboxDispatcher dispatcher = mock(EventOutboxDispatcher.class);
        when(outbox.findByPublishedAtIsNullOrderByCreatedAtAsc(PageRequest.of(0, 50)))
                .thenReturn(List.of(new EventOutboxEntity("first", "{}"),
                        new EventOutboxEntity("second", "{}")));
        doThrow(new IllegalStateException("Kafka indisponible"))
                .when(dispatcher).dispatch("first");

        new EventOutboxWorker(outbox, dispatcher).publishPending();

        verify(dispatcher).dispatch("first");
        verify(dispatcher).dispatch("second");
    }
}
