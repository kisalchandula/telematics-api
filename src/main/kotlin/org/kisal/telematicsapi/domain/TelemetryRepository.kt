package org.kisal.telematicsapi.domain

interface TelemetryRepository {

    fun save(event: TelemetryEvent)

    fun findLatestByImei(imei: String): TelemetryEvent?

    fun findByImei(imei: String): List<TelemetryEvent>
}