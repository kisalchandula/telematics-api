package org.kisal.telematicsapi.domain

data class Device(
    val id: Long,
    val imei: String,
    val manufacturer: String?,
    val model: String?,
    val name: String?,
    val active: Boolean
)