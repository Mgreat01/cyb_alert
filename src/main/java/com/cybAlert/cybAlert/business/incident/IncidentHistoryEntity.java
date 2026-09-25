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
@Table(name = "incident_history")
public class IncidentHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(nullable = false, length = 50)
    private String action;
    @Column(nullable = false, length = 4000)
    private String detail;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IncidentHistoryEntity() {
    }

    public IncidentHistoryEntity(UUID incidentId, UUID userId, String action, String detail) {
        this.incidentId = incidentId;
        this.userId = userId;
        this.action = action;
        this.detail = detail;
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getIncidentId() { return incidentId; }
    public UUID getUserId() { return userId; }
    public String getAction() { return action; }
    public String getDetail() { return detail; }
    public Instant getCreatedAt() { return createdAt; }
}
