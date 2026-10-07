CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE IF NOT EXISTS devices (
    id BIGSERIAL PRIMARY KEY,
    imei VARCHAR(20) NOT NULL UNIQUE,
    manufacturer VARCHAR(100),
    model VARCHAR(100),
    name VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS device_state (
    device_id BIGINT PRIMARY KEY
    REFERENCES devices(id)
    ON DELETE CASCADE,

    last_seen TIMESTAMPTZ,

    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    altitude INTEGER,
    speed INTEGER,
    heading INTEGER,
    satellites INTEGER,

    connection_status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS telemetry_events (
    id BIGSERIAL,

    device_id BIGINT NOT NULL
    REFERENCES devices(id)
    ON DELETE CASCADE,

    event_time TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    altitude INTEGER NOT NULL,
    speed INTEGER NOT NULL,
    heading INTEGER NOT NULL,
    satellites INTEGER NOT NULL,

    io_data JSONB,

    PRIMARY KEY (id, event_time)
    );

SELECT create_hypertable(
               'telemetry_events',
               by_range('event_time'),
               if_not_exists => TRUE
       );

CREATE INDEX IF NOT EXISTS idx_telemetry_device_time
    ON telemetry_events (device_id, event_time DESC);