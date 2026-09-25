package com.cybAlert.cybAlert.business.incident;

import com.cybAlert.cybAlert.business.alert.AlertEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "incidents")
public class IncidentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;
    @Column(name = "assigned_to")
    private UUID assignedTo;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, length = 4000)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertEntity.Severity severity;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "resolved_at")
    private Instant resolvedAt;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "incident_alerts", joinColumns = @JoinColumn(name = "incident_id"),
            inverseJoinColumns = @JoinColumn(name = "alert_id"))
    private Set<AlertEntity> alerts = new HashSet<>();

    protected IncidentEntity() {
    }

    public IncidentEntity(UUID createdBy, String title, String description,
                          AlertEntity.Severity severity, Priority priority,
                          Set<AlertEntity> alerts) {
        this.createdBy = createdBy;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.priority = priority;
        this.alerts.addAll(alerts);
        status = Status.OPEN;
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    public void changeStatus(Status next) {
        status = next;
        updatedAt = Instant.now();
        if (next == Status.RESOLVED || next == Status.CLOSED) {
            resolvedAt = updatedAt;
        }
    }

    public void assign(UUID userId) {
        assignedTo = userId;
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCreatedBy() { return createdBy; }
    public UUID getAssignedTo() { return assignedTo; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public AlertEntity.Severity getSeverity() { return severity; }
    public Priority getPriority() { return priority; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public Set<AlertEntity> getAlerts() { return alerts; }

    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Status { OPEN, IN_PROGRESS, CONTAINED, RESOLVED, CLOSED, FALSE_POSITIVE }
}
