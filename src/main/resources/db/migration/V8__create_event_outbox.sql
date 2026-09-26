CREATE TABLE event_outbox (
    event_id VARCHAR(100) PRIMARY KEY,
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_event_outbox_pending ON event_outbox (published_at, created_at);
