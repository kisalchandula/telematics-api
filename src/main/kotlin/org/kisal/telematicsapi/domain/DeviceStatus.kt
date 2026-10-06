package org.kisal.telematicsapi.domain

import java.time.Instant

data class DeviceStatus(
    val imei: String,
    val status: String,
    val lastSeen: Instant?
)