package org.kisal.telematicsapi.domain

import org.kisal.telematicsapi.protocol.IoElement
import java.time.Instant

data class TelemetryEvent(
    val deviceId: Long,
    val timestamp: Instant,
    val receivedAt: Instant,
    val latitude: Double,
    val longitude: Double,
    val altitude: Int,
    val heading: Int,
    val satellites: Int,
    val speed: Int,
    val ioElements: List<IoElement> = emptyList()
)