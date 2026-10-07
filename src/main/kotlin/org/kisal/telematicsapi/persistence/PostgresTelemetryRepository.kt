package org.kisal.telematicsapi.persistence

import org.kisal.telematicsapi.domain.DeviceInfo
import org.kisal.telematicsapi.domain.DeviceStatus
import org.kisal.telematicsapi.domain.TelemetryEvent
import org.kisal.telematicsapi.domain.TelemetryRepository
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Instant
import javax.sql.DataSource

@Repository
class PostgresTelemetryRepository(
    private val dataSource: DataSource
) : TelemetryRepository {

    override fun save(event: TelemetryEvent) {

        val insertTelemetrySql = """
        INSERT INTO telemetry_events (
            device_id,
            event_time,
            received_at,
            latitude,
            longitude,
            altitude,
            heading,
            satellites,
            speed
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """.trimIndent()

        val updateStateSql = """
        INSERT INTO device_state (
            device_id,
            last_seen,
            latitude,
            longitude,
            altitude,
            speed,
            heading,
            satellites,
            connection_status,
            updated_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ONLINE', NOW())
        ON CONFLICT (device_id)
        DO UPDATE SET
            last_seen = EXCLUDED.last_seen,
            latitude = EXCLUDED.latitude,
            longitude = EXCLUDED.longitude,
            altitude = EXCLUDED.altitude,
            speed = EXCLUDED.speed,
            heading = EXCLUDED.heading,
            satellites = EXCLUDED.satellites,
            connection_status = 'ONLINE',
            updated_at = NOW()
    """.trimIndent()

        dataSource.connection.use { connection ->

            connection.autoCommit = false

            try {

                connection.prepareStatement(
                    insertTelemetrySql
                ).use { statement ->

                    statement.setLong(
                        1,
                        event.deviceId
                    )

                    statement.setTimestamp(
                        2,
                        Timestamp.from(event.timestamp)
                    )

                    statement.setTimestamp(
                        3,
                        Timestamp.from(event.receivedAt)
                    )

                    statement.setDouble(
                        4,
                        event.latitude
                    )

                    statement.setDouble(
                        5,
                        event.longitude
                    )

                    statement.setInt(
                        6,
                        event.altitude
                    )

                    statement.setInt(
                        7,
                        event.heading
                    )

                    statement.setInt(
                        8,
                        event.satellites
                    )

                    statement.setInt(
                        9,
                        event.speed
                    )

                    statement.executeUpdate()
                }

                connection.prepareStatement(
                    updateStateSql
                ).use { statement ->

                    statement.setLong(
                        1,
                        event.deviceId
                    )

                    statement.setTimestamp(
                        2,
                        Timestamp.from(event.timestamp)
                    )

                    statement.setDouble(
                        3,
                        event.latitude
                    )

                    statement.setDouble(
                        4,
                        event.longitude
                    )

                    statement.setInt(
                        5,
                        event.altitude
                    )

                    statement.setInt(
                        6,
                        event.speed
                    )

                    statement.setInt(
                        7,
                        event.heading
                    )

                    statement.setInt(
                        8,
                        event.satellites
                    )

                    statement.executeUpdate()
                }

                connection.commit()

            } catch (exception: Exception) {

                connection.rollback()

                throw exception
            }
        }
    }

    override fun findLatestByImei(
        imei: String
    ): TelemetryEvent? {

        val sql = """
        SELECT
            s.device_id,
            s.last_seen,
            s.latitude,
            s.longitude,
            s.altitude,
            s.heading,
            s.satellites,
            s.speed
        FROM device_state s
        JOIN devices d
            ON d.id = s.device_id
        WHERE d.imei = ?
    """.trimIndent()

        dataSource.connection.use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.setString(1, imei)

                statement.executeQuery().use { resultSet ->

                    if (!resultSet.next()) {
                        return null
                    }

                    val timestamp =
                        resultSet
                            .getTimestamp("last_seen")
                            .toInstant()

                    return TelemetryEvent(
                        deviceId =
                            resultSet.getLong("device_id"),

                        timestamp = timestamp,

                        receivedAt = timestamp,

                        latitude =
                            resultSet.getDouble("latitude"),

                        longitude =
                            resultSet.getDouble("longitude"),

                        altitude =
                            resultSet.getInt("altitude"),

                        heading =
                            resultSet.getInt("heading"),

                        satellites =
                            resultSet.getInt("satellites"),

                        speed =
                            resultSet.getInt("speed")
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
                t.device_id,
                t.event_time,
                t.received_at,
                t.latitude,
                t.longitude,
                t.altitude,
                t.heading,
                t.satellites,
                t.speed
            FROM telemetry_events t
            JOIN devices d
                ON d.id = t.device_id
            WHERE d.imei = ?
            ORDER BY t.event_time DESC
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
                            mapTelemetryEvent(resultSet)
                        )
                    }

                    return events
                }
            }
        }
    }

    override fun findAllImeis(): List<String> {

        val sql = """
            SELECT imei
            FROM devices
            WHERE active = TRUE
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

    override fun findDeviceInfo(
        imei: String
    ): DeviceInfo? {

        val sql = """
        SELECT
            s.device_id,
            s.last_seen,
            s.latitude,
            s.longitude,
            s.altitude,
            s.heading,
            s.satellites,
            s.speed
        FROM device_state s
        JOIN devices d
            ON d.id = s.device_id
        WHERE d.imei = ?
    """.trimIndent()

        dataSource.connection.use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.setString(1, imei)

                statement.executeQuery().use { resultSet ->

                    if (!resultSet.next()) {
                        return null
                    }

                    val timestamp =
                        resultSet
                            .getTimestamp("last_seen")
                            .toInstant()

                    val latestTelemetry =
                        TelemetryEvent(
                            deviceId =
                                resultSet.getLong("device_id"),

                            timestamp = timestamp,

                            receivedAt = timestamp,

                            latitude =
                                resultSet.getDouble("latitude"),

                            longitude =
                                resultSet.getDouble("longitude"),

                            altitude =
                                resultSet.getInt("altitude"),

                            heading =
                                resultSet.getInt("heading"),

                            satellites =
                                resultSet.getInt("satellites"),

                            speed =
                                resultSet.getInt("speed")
                        )

                    return DeviceInfo(
                        imei = imei,
                        lastSeen = timestamp,
                        latestTelemetry = latestTelemetry
                    )
                }
            }
        }
    }

    override fun findDeviceStatus(
        imei: String
    ): DeviceStatus? {

        val sql = """
        SELECT
            s.last_seen,
            s.connection_status
        FROM device_state s
        JOIN devices d
            ON d.id = s.device_id
        WHERE d.imei = ?
    """.trimIndent()

        dataSource.connection.use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.setString(1, imei)

                statement.executeQuery().use { resultSet ->

                    if (!resultSet.next()) {
                        return null
                    }

                    val lastSeen =
                        resultSet
                            .getTimestamp("last_seen")
                            .toInstant()

                    val status =
                        resultSet.getString(
                            "connection_status"
                        )

                    return DeviceStatus(
                        imei = imei,
                        status = status,
                        lastSeen = lastSeen
                    )
                }
            }
        }
    }

    private fun mapTelemetryEvent(
        resultSet: java.sql.ResultSet
    ): TelemetryEvent {

        return TelemetryEvent(
            deviceId = resultSet.getLong("device_id"),

            timestamp = resultSet
                .getTimestamp("event_time")
                .toInstant(),

            receivedAt = resultSet
                .getTimestamp("received_at")
                .toInstant(),

            latitude = resultSet.getDouble("latitude"),
            longitude = resultSet.getDouble("longitude"),
            altitude = resultSet.getInt("altitude"),
            heading = resultSet.getInt("heading"),
            satellites = resultSet.getInt("satellites"),
            speed = resultSet.getInt("speed")
        )
    }

    override fun markOffline(imei: String) {

        val sql = """
        UPDATE device_state
        SET
            connection_status = 'OFFLINE',
            updated_at = NOW()
        FROM devices
        WHERE device_state.device_id = devices.id
          AND devices.imei = ?
    """.trimIndent()

        dataSource.connection.use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.setString(1, imei)

                statement.executeUpdate()
            }
        }
    }
}