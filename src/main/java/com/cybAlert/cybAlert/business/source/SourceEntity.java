package com.cybAlert.cybAlert.business.source;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sources")
public class SourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String hostname;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "mac_address", length = 17)
    private String macAddress;

    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private Type type;

    @Column(nullable = false, length = 50)
    private String environment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SourceEntity() {
    }

    public SourceEntity(String hostname, String ipAddress, String macAddress,
                        String operatingSystem, Type type, String environment) {
        update(hostname, ipAddress, macAddress, operatingSystem, type, environment);
        status = Status.UNKNOWN;
    }

    public void update(String hostname, String ipAddress, String macAddress,
                       String operatingSystem, Type type, String environment) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.operatingSystem = operatingSystem;
        this.type = type;
        this.environment = environment;
    }

    public void heartbeat() {
        lastSeenAt = Instant.now();
        status = Status.ONLINE;
    }

    public void changeStatus(Status status) {
        this.status = status;
    }

    @PrePersist
    void initializeCreationDate() {
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getHostname() { return hostname; }
    public String getIpAddress() { return ipAddress; }
    public String getMacAddress() { return macAddress; }
    public String getOperatingSystem() { return operatingSystem; }
    public Type getType() { return type; }
    public String getEnvironment() { return environment; }
    public Status getStatus() { return status; }
    public Instant getLastSeenAt() { return lastSeenAt; }
    public Instant getCreatedAt() { return createdAt; }

    public enum Type { SERVER, WORKSTATION, APPLICATION, NETWORK_DEVICE, AGENT }
    public enum Status { ONLINE, OFFLINE, UNKNOWN, DISABLED }
}
