package org.kisal.telematicsapi.domain

import org.kisal.telematicsapi.protocol.AvlRecord
import java.time.Instant

object TelemetryEventMapper {

    fun map(
        deviceId: Long,
        record: AvlRecord,
        receivedAt: Instant = Instant.now()
    ): TelemetryEvent {

        val gps = record.gps

        return TelemetryEvent(
            deviceId = deviceId,
            timestamp = Instant.ofEpochMilli(gps.timestamp),
            receivedAt = receivedAt,
            latitude = gps.latitude,
            longitude = gps.longitude,
            altitude = gps.altitude,
            heading = gps.angle,
            satellites = gps.satellites,
            speed = gps.speed,
            ioElements = record.ioElements
        )
    }
}