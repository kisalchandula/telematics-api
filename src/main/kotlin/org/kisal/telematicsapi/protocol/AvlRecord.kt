package org.kisal.telematicsapi.protocol

import protocol.GpsData

data class AvlRecord(
    val gps: GpsData,
    val eventId: Int = 0,
    val ioElements: List<IoElement> = emptyList()
)