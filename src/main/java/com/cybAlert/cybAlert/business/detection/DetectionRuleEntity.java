package com.cybAlert.cybAlert.business.detection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "detection_rules")
public class DetectionRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    @Column(name = "threshold_count", nullable = false)
    private int thresholdCount;
    @Column(name = "window_seconds", nullable = false)
    private int windowSeconds;
    @Column(nullable = false, length = 20)
    private String severity;
    @Column(nullable = false)
    private boolean enabled;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DetectionRuleEntity() {
    }

    public DetectionRuleEntity(String name, String eventType, int thresholdCount,
                               int windowSeconds, String severity, boolean enabled) {
        update(name, eventType, thresholdCount, windowSeconds, severity, enabled);
    }

    public void update(String name, String eventType, int thresholdCount,
                       int windowSeconds, String severity, boolean enabled) {
        this.name = name;
        this.eventType = eventType;
        this.thresholdCount = thresholdCount;
        this.windowSeconds = windowSeconds;
        this.severity = severity;
        this.enabled = enabled;
    }

    @PrePersist
    void initializeDates() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void updateDate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEventType() { return eventType; }
    public int getThresholdCount() { return thresholdCount; }
    public int getWindowSeconds() { return windowSeconds; }
    public String getSeverity() { return severity; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
