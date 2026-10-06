package org.kisal.telematicsapi.api

import org.kisal.telematicsapi.domain.DeviceInfo
import org.kisal.telematicsapi.domain.TelemetryEvent
import org.kisal.telematicsapi.domain.TelemetryRepository
import org.kisal.telematicsapi.domain.DeviceStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/devices")
class TelemetryController(
    private val repository: TelemetryRepository
) {

    @GetMapping("/{imei}/latest")
    fun getLatestTelemetry(
        @PathVariable imei: String
    ): TelemetryEvent? {
        return repository.findLatestByImei(imei)
    }

    @GetMapping("/{imei}/telemetry")
    fun getTelemetry(
        @PathVariable imei: String,
        @RequestParam(defaultValue = "100") limit: Int
    ): List<TelemetryEvent> {
        return repository.findByImei(imei, limit)
    }

    @GetMapping
    fun getDevices(): List<String> {
        return repository.findAllImeis()
    }

    @GetMapping("/{imei}")
    fun getDevice(
        @PathVariable imei: String
    ): DeviceInfo? {
        return repository.findDeviceInfo(imei)
    }

    @GetMapping("/{imei}/status")
    fun getDeviceStatus(
        @PathVariable imei: String
    ): DeviceStatus? {
        return repository.findDeviceStatus(imei)
    }
}