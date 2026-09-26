package com.cybAlert.cybAlert.business.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "event_outbox")
public class EventOutboxEntity {

    @Id
    @Column(name = "event_id", length = 100)
    private String eventId;
    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "published_at")
    private Instant publishedAt;

    protected EventOutboxEntity() { }

    public EventOutboxEntity(String eventId, String payload) {
        this.eventId = eventId;
        this.payload = payload;
        this.createdAt = Instant.now();
    }

    public String getEventId() { return eventId; }
    public String getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public void markPublished() { publishedAt = Instant.now(); }
}
