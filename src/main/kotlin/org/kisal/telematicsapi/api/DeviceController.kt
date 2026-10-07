package org.kisal.telematicsapi.api

import org.springframework.web.bind.annotation.GetMapping
import org.kisal.telematicsapi.device.DeviceService
import org.kisal.telematicsapi.domain.Device
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable

@RestController
@RequestMapping("/api/devices")
class DeviceController(
    private val deviceService: DeviceService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDevice(
        @RequestBody request: CreateDeviceRequest
    ): Device {
        return deviceService.createDevice(request)
    }

    @GetMapping
    fun listDevices(): List<Device> {
        return deviceService.listDevices()
    }

    @DeleteMapping("/{imei}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDevice(
        @PathVariable imei: String
    ) {
        deviceService.deleteDevice(imei)
    }
}