package com.cybAlert.cybAlert.business.event;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Document(indexName = "security-events", createIndex = false)
public class SecurityEvent {

    @Id
    private String eventId;
    @Field(type = FieldType.Keyword)
    private String eventType;
    @Field(type = FieldType.Date)
    private Instant timestamp;
    @Field(type = FieldType.Keyword)
    private UUID sourceId;
    @Field(type = FieldType.Ip)
    private String sourceIp;
    @Field(type = FieldType.Ip)
    private String destinationIp;
    private Integer sourcePort;
    private Integer destinationPort;
    @Field(type = FieldType.Keyword)
    private String protocol;
    @Field(type = FieldType.Keyword)
    private String username;
    @Field(type = FieldType.Keyword)
    private String severity;
    @Field(type = FieldType.Text)
    private String message;
    @Field(type = FieldType.Object)
    private Map<String, Object> metadata;

    protected SecurityEvent() {
    }

    public SecurityEvent(String eventId, String eventType, Instant timestamp, UUID sourceId,
                         String sourceIp, String destinationIp, Integer sourcePort,
                         Integer destinationPort, String protocol, String username,
                         String severity, String message, Map<String, Object> metadata) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.sourceId = sourceId;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.protocol = protocol;
        this.username = username;
        this.severity = severity;
        this.message = message;
        this.metadata = metadata;
    }

    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public Instant getTimestamp() { return timestamp; }
    public UUID getSourceId() { return sourceId; }
    public String getSourceIp() { return sourceIp; }
    public String getDestinationIp() { return destinationIp; }
    public Integer getSourcePort() { return sourcePort; }
    public Integer getDestinationPort() { return destinationPort; }
    public String getProtocol() { return protocol; }
    public String getUsername() { return username; }
    public String getSeverity() { return severity; }
    public String getMessage() { return message; }
    public Map<String, Object> getMetadata() { return metadata; }
}
