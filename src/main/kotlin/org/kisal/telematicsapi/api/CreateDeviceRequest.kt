package org.kisal.telematicsapi.api

data class CreateDeviceRequest(
    val imei: String,
    val manufacturer: String?,
    val model: String?,
    val name: String?
)