CREATE TABLE sources (
    id UUID PRIMARY KEY,
    hostname VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(45) NOT NULL,
    mac_address VARCHAR(17),
    operating_system VARCHAR(100),
    source_type VARCHAR(30) NOT NULL,
    environment VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    last_seen_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_sources_type CHECK (
        source_type IN ('SERVER', 'WORKSTATION', 'APPLICATION', 'NETWORK_DEVICE', 'AGENT')
    ),
    CONSTRAINT ck_sources_status CHECK (
        status IN ('ONLINE', 'OFFLINE', 'UNKNOWN', 'DISABLED')
    )
);
