package org.kisal.telematicsapi.persistence

import org.kisal.telematicsapi.domain.Device
import org.kisal.telematicsapi.domain.DeviceRepository
import org.springframework.stereotype.Repository
import java.sql.Connection
import javax.sql.DataSource

@Repository
class PostgresDeviceRepository(
    private val dataSource: DataSource
) : DeviceRepository {

    override fun findByImei(imei: String): Device? {

        val sql = """
            SELECT
                id,
                imei,
                manufacturer,
                model,
                name,
                active
            FROM devices
            WHERE imei = ?
        """.trimIndent()

        dataSource.connection.use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.setString(1, imei)

                statement.executeQuery().use { resultSet ->

                    if (!resultSet.next()) {
                        return null
                    }

                    return Device(
                        id = resultSet.getLong("id"),
                        imei = resultSet.getString("imei"),
                        manufacturer = resultSet.getString("manufacturer"),
                        model = resultSet.getString("model"),
                        name = resultSet.getString("name"),
                        active = resultSet.getBoolean("active")
                    )
                }
            }
        }
    }

    override fun save(device: Device): Device {

        val sql = """
        INSERT INTO devices (
            imei,
            manufacturer,
            model,
            name,
            active
        )
        VALUES (?, ?, ?, ?, ?)
        RETURNING
            id,
            imei,
            manufacturer,
            model,
            name,
            active
    """.trimIndent()

        dataSource.connection.use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.setString(1, device.imei)
                statement.setString(2, device.manufacturer)
                statement.setString(3, device.model)
                statement.setString(4, device.name)
                statement.setBoolean(5, device.active)

                statement.executeQuery().use { resultSet ->

                    if (!resultSet.next()) {
                        throw IllegalStateException(
                            "Failed to create device"
                        )
                    }

                    return Device(
                        id = resultSet.getLong("id"),
                        imei = resultSet.getString("imei"),
                        manufacturer =
                            resultSet.getString("manufacturer"),
                        model =
                            resultSet.getString("model"),
                        name =
                            resultSet.getString("name"),
                        active =
                            resultSet.getBoolean("active")
                    )
                }
            }
        }
    }

    override fun findAll(): List<Device> {

        val sql = """
            SELECT
                id,
                imei,
                manufacturer,
                model,
                name,
                active
            FROM devices
            WHERE active = TRUE
            ORDER BY id
        """.trimIndent()

        dataSource.connection.use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.executeQuery().use { resultSet ->

                    val devices = mutableListOf<Device>()

                    while (resultSet.next()) {

                        devices.add(
                            Device(
                                id = resultSet.getLong("id"),
                                imei = resultSet.getString("imei"),
                                manufacturer =
                                    resultSet.getString("manufacturer"),
                                model =
                                    resultSet.getString("model"),
                                name =
                                    resultSet.getString("name"),
                                active =
                                    resultSet.getBoolean("active")
                            )
                        )
                    }

                    return devices
                }
            }
        }
    }

    override fun deleteByImei(imei: String): Boolean {

        val sql = """
        UPDATE devices
        SET active = FALSE
        WHERE imei = ?
          AND active = TRUE
    """.trimIndent()

        dataSource.connection.use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.setString(1, imei)

                return statement.executeUpdate() > 0
            }
        }
    }
}