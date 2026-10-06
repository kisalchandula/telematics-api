package org.kisal.telematicsapi.persistence

import org.kisal.telematicsapi.domain.DeviceInfo
import org.kisal.telematicsapi.domain.DeviceStatus
import org.kisal.telematicsapi.domain.TelemetryEvent
import org.kisal.telematicsapi.domain.TelemetryRepository
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import javax.sql.DataSource
import java.time.Instant

@Repository
class PostgresTelemetryRepository(
    private val dataSource: DataSource
) : TelemetryRepository {

    override fun save(event: TelemetryEvent) {

        val sql = """
            INSERT INTO telemetry_events (
                imei,
                timestamp,
                latitude,
                longitude,
                altitude,
                angle,
                satellites,
                speed
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        dataSource.connection.use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.setString(1, event.imei)
                statement.setTimestamp(
                    2,
                    Timestamp.from(event.timestamp)
                )
                statement.setDouble(3, event.latitude)
                statement.setDouble(4, event.longitude)
                statement.setInt(5, event.altitude)
                statement.setInt(6, event.angle)
                statement.setInt(7, event.satellites)
                statement.setInt(8, event.speed)

                statement.executeUpdate()
            }
        }
    }

    override fun findLatestByImei(imei: String): TelemetryEvent? {

        val sql = """
        SELECT
            imei,
            timestamp,
            latitude,
            longitude,
            altitude,
            angle,
            satellites,
            speed
        FROM telemetry_events
        WHERE imei = ?
        ORDER BY timestamp DESC
        LIMIT 1
    """.trimIndent()

        dataSource.connection.use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.setString(1, imei)

                statement.executeQuery().use { resultSet ->

                    if (!resultSet.next()) {
                        return null
                    }

                    return TelemetryEvent(
                        imei = resultSet.getString("imei"),
                        timestamp = resultSet
                            .getTimestamp("timestamp")
                            .toInstant(),
                        latitude = resultSet.getDouble("latitude"),
                        longitude = resultSet.getDouble("longitude"),
                        altitude = resultSet.getInt("altitude"),
                        angle = resultSet.getInt("angle"),
                        satellites = resultSet.getInt("satellites"),
                        speed = resultSet.getInt("speed")
                    )
                }
            }
        }
    }

    override fun findByImei(
        imei: String,
        limit: Int
    ): List<TelemetryEvent> {

        val sql = """
        SELECT
            imei,
            timestamp,
            latitude,
            longitude,
            altitude,
            angle,
            satellites,
            speed
        FROM telemetry_events
        WHERE imei = ?
        ORDER BY timestamp DESC
        LIMIT ?
    """.trimIndent()

        dataSource.connection.use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.setString(1, imei)
                statement.setInt(2, limit)

                statement.executeQuery().use { resultSet ->

                    val events = mutableListOf<TelemetryEvent>()

                    while (resultSet.next()) {

                        events.add(
                            TelemetryEvent(
                                imei = resultSet.getString("imei"),
                                timestamp = resultSet
                                    .getTimestamp("timestamp")
                                    .toInstant(),
                                latitude = resultSet.getDouble("latitude"),
                                longitude = resultSet.getDouble("longitude"),
                                altitude = resultSet.getInt("altitude"),
                                angle = resultSet.getInt("angle"),
                                satellites = resultSet.getInt("satellites"),
                                speed = resultSet.getInt("speed")
                            )
                        )
                    }

                    return events
                }
            }
        }
    }

    override fun findAllImeis(): List<String> {

        val sql = """
        SELECT DISTINCT imei
        FROM telemetry_events
        ORDER BY imei
    """.trimIndent()

        dataSource.connection.use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.executeQuery().use { resultSet ->

                    val imeis = mutableListOf<String>()

                    while (resultSet.next()) {
                        imeis.add(
                            resultSet.getString("imei")
                        )
                    }

                    return imeis
                }
            }
        }
    }

    override fun findDeviceInfo(imei: String): DeviceInfo? {

        val sql = """
        SELECT
            imei,
            timestamp,
            latitude,
            longitude,
            altitude,
            angle,
            satellites,
            speed
        FROM telemetry_events
        WHERE imei = ?
        ORDER BY timestamp DESC
        LIMIT 1
    """.trimIndent()

        dataSource.connection.use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.setString(1, imei)

                statement.executeQuery().use { resultSet ->

                    if (!resultSet.next()) {
                        return null
                    }

                    val latestTelemetry = TelemetryEvent(
                        imei = resultSet.getString("imei"),
                        timestamp = resultSet
                            .getTimestamp("timestamp")
                            .toInstant(),
                        latitude = resultSet.getDouble("latitude"),
                        longitude = resultSet.getDouble("longitude"),
                        altitude = resultSet.getInt("altitude"),
                        angle = resultSet.getInt("angle"),
                        satellites = resultSet.getInt("satellites"),
                        speed = resultSet.getInt("speed")
                    )

                    return DeviceInfo(
                        imei = imei,
                        lastSeen = latestTelemetry.timestamp,
                        latestTelemetry = latestTelemetry
                    )
                }
            }
        }
    }

    override fun findDeviceStatus(imei: String): DeviceStatus? {

        val sql = """
        SELECT timestamp
        FROM telemetry_events
        WHERE imei = ?
        ORDER BY timestamp DESC
        LIMIT 1
    """.trimIndent()

        dataSource.connection.use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.setString(1, imei)

                statement.executeQuery().use { resultSet ->

                    if (!resultSet.next()) {
                        return null
                    }

                    val lastSeen = resultSet
                        .getTimestamp("timestamp")
                        .toInstant()

                    val status = if (
                        lastSeen.isAfter(
                            Instant.now().minusSeconds(60)
                        )
                    ) {
                        "ONLINE"
                    } else {
                        "OFFLINE"
                    }

                    return DeviceStatus(
                        imei = imei,
                        status = status,
                        lastSeen = lastSeen
                    )
                }
            }
        }
    }
}