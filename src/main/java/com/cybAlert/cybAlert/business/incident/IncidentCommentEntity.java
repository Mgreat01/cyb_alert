package com.cybAlert.cybAlert.business.incident;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incident_comments")
public class IncidentCommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(nullable = false, length = 4000)
    private String content;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IncidentCommentEntity() {
    }

    public IncidentCommentEntity(UUID incidentId, UUID userId, String content) {
        this.incidentId = incidentId;
        this.userId = userId;
        this.content = content;
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getIncidentId() { return incidentId; }
    public UUID getUserId() { return userId; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}
