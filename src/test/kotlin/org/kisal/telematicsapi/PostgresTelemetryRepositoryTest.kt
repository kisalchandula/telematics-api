package org.kisal.telematicsapi

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.kisal.telematicsapi.domain.TelemetryEvent
import org.kisal.telematicsapi.persistence.PostgresTelemetryRepository
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.testcontainers.containers.PostgreSQLContainer
import java.time.Instant

class PostgresTelemetryRepositoryTest {

    companion object {

        private val postgres =
            PostgreSQLContainer("postgres:16-alpine").apply {

                start()

                DriverManagerDataSource(
                    jdbcUrl,
                    username,
                    password
                ).connection.use { connection ->

                    connection.createStatement().use { statement ->

                        statement.execute(
                            """
                            CREATE TABLE devices (
                                id BIGSERIAL PRIMARY KEY,
                                imei VARCHAR(20) NOT NULL UNIQUE,
                                manufacturer VARCHAR(100),
                                model VARCHAR(100),
                                name VARCHAR(100),
                                active BOOLEAN NOT NULL DEFAULT TRUE,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                            );

                            CREATE TABLE device_state (
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

                                connection_status VARCHAR(20)
                                    NOT NULL DEFAULT 'OFFLINE',

                                updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                            );

                            CREATE TABLE telemetry_events (
                                id BIGSERIAL PRIMARY KEY,

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

                                io_data JSONB
                            );
                            """.trimIndent()
                        )

                        statement.execute(
                            """
                            INSERT INTO devices (
                                imei,
                                manufacturer,
                                model,
                                name,
                                active
                            )
                            VALUES (
                                '123456789012345',
                                'Teltonika',
                                'Test',
                                'Test Device',
                                TRUE
                            )
                            """.trimIndent()
                        )
                    }
                }
            }
    }

    @Test
    fun `save and retrieve latest telemetry event`() {

        val dataSource =
            DriverManagerDataSource(
                postgres.jdbcUrl,
                postgres.username,
                postgres.password
            )

        val repository =
            PostgresTelemetryRepository(dataSource)

        val event =
            TelemetryEvent(
                deviceId = 1L,
                timestamp =
                    Instant.parse(
                        "2026-10-02T10:00:00Z"
                    ),
                receivedAt =
                    Instant.parse(
                        "2026-10-02T10:00:05Z"
                    ),
                latitude = 49.0069,
                longitude = 8.4037,
                altitude = 120,
                heading = 90,
                satellites = 10,
                speed = 50
            )

        repository.save(event)

        val result =
            repository.findLatestByImei(
                "123456789012345"
            )

        assertNotNull(result)

        assertEquals(
            event.deviceId,
            result!!.deviceId
        )

        assertEquals(
            event.timestamp,
            result.timestamp
        )

        assertEquals(
            event.latitude,
            result.latitude
        )

        assertEquals(
            event.longitude,
            result.longitude
        )

        assertEquals(
            event.altitude,
            result.altitude
        )

        assertEquals(
            event.heading,
            result.heading
        )

        assertEquals(
            event.satellites,
            result.satellites
        )

        assertEquals(
            event.speed,
            result.speed
        )
    }
}