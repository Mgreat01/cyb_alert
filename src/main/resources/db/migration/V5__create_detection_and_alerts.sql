CREATE TABLE detection_rules (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    threshold_count INTEGER NOT NULL,
    window_seconds INTEGER NOT NULL,
    severity VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_detection_threshold CHECK (threshold_count > 0),
    CONSTRAINT ck_detection_window CHECK (window_seconds > 0)
);

CREATE TABLE detection_observations (
    event_id VARCHAR(100) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    source_ip VARCHAR(45),
    destination_ip VARCHAR(45),
    event_time TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_detection_observations_window
    ON detection_observations (event_type, source_ip, event_time);

CREATE TABLE alerts (
    id UUID PRIMARY KEY,
    rule_id UUID NOT NULL REFERENCES detection_rules(id),
    source_id UUID NOT NULL REFERENCES sources(id),
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    risk_score INTEGER NOT NULL,
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    resolved_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT ck_alert_risk_score CHECK (risk_score BETWEEN 0 AND 100)
);

CREATE INDEX idx_alerts_status_severity ON alerts (status, severity);

INSERT INTO detection_rules
    (id, name, event_type, threshold_count, window_seconds, severity, enabled,
     created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'BRUTE_FORCE', 'LOGIN_FAILED',
     5, 60, 'HIGH', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000102', 'NETWORK_SCAN', 'PORT_SCAN',
     10, 60, 'HIGH', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
