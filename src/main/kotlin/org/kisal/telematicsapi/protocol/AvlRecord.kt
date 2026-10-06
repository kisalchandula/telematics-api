package org.kisal.telematicsapi.protocol

data class AvlRecord(
    val gps: GpsData,
    val eventId: Int = 0,
    val ioElements: List<IoElement> = emptyList()
)