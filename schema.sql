CREATE TABLE IF NOT EXISTS telemetry_events (
    id BIGSERIAL PRIMARY KEY,
    imei VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    altitude INTEGER NOT NULL,
    angle INTEGER NOT NULL,
    satellites INTEGER NOT NULL,
    speed INTEGER NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_telemetry_events_imei_timestamp
    ON telemetry_events (imei, timestamp DESC);