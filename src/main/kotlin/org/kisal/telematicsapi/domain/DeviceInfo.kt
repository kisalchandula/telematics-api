package org.kisal.telematicsapi.domain

import java.time.Instant

data class DeviceInfo(
    val imei: String,
    val lastSeen: Instant?,
    val latestTelemetry: TelemetryEvent?
)