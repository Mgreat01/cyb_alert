package com.cybAlert.cybAlert.business.detection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "detection_observations")
public class DetectionObservationEntity {

    @Id
    @Column(name = "event_id", length = 100)
    private String eventId;
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    @Column(name = "source_ip", length = 45)
    private String sourceIp;
    @Column(name = "destination_ip", length = 45)
    private String destinationIp;
    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    protected DetectionObservationEntity() {
    }

    public DetectionObservationEntity(String eventId, String eventType, String sourceIp,
                                      String destinationIp, Instant eventTime) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.eventTime = eventTime;
    }

    public String getEventId() { return eventId; }
}
