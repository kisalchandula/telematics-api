package org.kisal.telematicsapi

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.kisal.telematicsapi.domain.TelemetryEvent
import org.kisal.telematicsapi.persistence.PostgresTelemetryRepository
import org.testcontainers.containers.PostgreSQLContainer
import org.springframework.jdbc.datasource.DriverManagerDataSource
import java.time.Instant

class PostgresTelemetryRepositoryTest {

    companion object {
        private val postgres = PostgreSQLContainer("postgres:16-alpine").apply {
            start()

            DriverManagerDataSource(
                jdbcUrl,
                username,
                password
            ).connection.use { connection ->

                connection.createStatement().use { statement ->
                    statement.execute(
                        """
                        CREATE TABLE telemetry_events (
                            id BIGSERIAL PRIMARY KEY,
                            imei VARCHAR(20) NOT NULL,
                            timestamp TIMESTAMP NOT NULL,
                            latitude DOUBLE PRECISION NOT NULL,
                            longitude DOUBLE PRECISION NOT NULL,
                            altitude INTEGER NOT NULL,
                            angle INTEGER NOT NULL,
                            satellites INTEGER NOT NULL,
                            speed INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                }
            }
        }
    }

    @Test
    fun `save and retrieve latest telemetry event`() {

        val dataSource = DriverManagerDataSource(
            postgres.jdbcUrl,
            postgres.username,
            postgres.password
        )

        val repository = PostgresTelemetryRepository(dataSource)

        val event = TelemetryEvent(
            imei = "123456789012345",
            timestamp = Instant.parse("2026-10-02T10:00:00Z"),
            latitude = 49.0069,
            longitude = 8.4037,
            altitude = 120,
            angle = 90,
            satellites = 10,
            speed = 50
        )

        repository.save(event)

        val result = repository.findLatestByImei("123456789012345")

        assertNotNull(result)
        assertEquals(event.imei, result!!.imei)
        assertEquals(event.latitude, result.latitude)
        assertEquals(event.longitude, result.longitude)
        assertEquals(event.speed, result.speed)
    }
}