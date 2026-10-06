package org.kisal.telematicsapi.domain

import org.kisal.telematicsapi.protocol.IoElement
import java.time.Instant

data class TelemetryEvent(
    val imei: String,
    val timestamp: Instant,
    val latitude: Double,
    val longitude: Double,
    val altitude: Int,
    val angle: Int,
    val satellites: Int,
    val speed: Int,
    val ioElements: List<IoElement> = emptyList()
)