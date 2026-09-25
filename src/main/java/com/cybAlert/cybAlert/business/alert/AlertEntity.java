package com.cybAlert.cybAlert.business.alert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alerts")
public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;
    @Column(name = "source_id", nullable = false)
    private UUID sourceId;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, length = 2000)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;
    @Column(name = "risk_score", nullable = false)
    private int riskScore;
    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;
    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected AlertEntity() {
    }

    public AlertEntity(UUID ruleId, UUID sourceId, String title, String description,
                       Severity severity, int riskScore, Instant detectedAt) {
        this.ruleId = ruleId;
        this.sourceId = sourceId;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.riskScore = Math.max(0, Math.min(100, riskScore));
        this.detectedAt = detectedAt;
        this.status = Status.DETECTED;
    }

    public void changeStatus(Status next) {
        if (next.ordinal() < status.ordinal()) {
            throw new IllegalStateException("Une alerte ne peut pas revenir à un état antérieur");
        }
        status = next;
        if (next == Status.ACKNOWLEDGED) { acknowledgedAt = Instant.now(); }
        if (next == Status.RESOLVED) { resolvedAt = Instant.now(); }
    }

    public UUID getId() { return id; }
    public UUID getRuleId() { return ruleId; }
    public UUID getSourceId() { return sourceId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Severity getSeverity() { return severity; }
    public Status getStatus() { return status; }
    public int getRiskScore() { return riskScore; }
    public Instant getDetectedAt() { return detectedAt; }
    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public Instant getResolvedAt() { return resolvedAt; }

    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Status { DETECTED, ACKNOWLEDGED, INVESTIGATING, RESOLVED }
}
