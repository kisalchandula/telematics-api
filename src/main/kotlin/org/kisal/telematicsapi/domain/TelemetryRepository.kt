package org.kisal.telematicsapi.domain

interface TelemetryRepository {

    fun save(event: TelemetryEvent)

    fun findLatestByImei(imei: String): TelemetryEvent?

    fun findByImei(
        imei: String,
        limit: Int
    ): List<TelemetryEvent>

    fun findAllImeis(): List<String>

    fun findDeviceInfo(imei: String): DeviceInfo?

    fun findDeviceStatus(imei: String): DeviceStatus?
}