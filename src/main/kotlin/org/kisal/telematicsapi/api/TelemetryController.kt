package org.kisal.telematicsapi.api

import org.kisal.telematicsapi.domain.TelemetryEvent
import org.kisal.telematicsapi.domain.TelemetryRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
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
}