package org.kisal.telematicsapi.device

import org.kisal.telematicsapi.api.CreateDeviceRequest
import org.kisal.telematicsapi.domain.Device
import org.kisal.telematicsapi.domain.DeviceRepository
import org.springframework.stereotype.Service

@Service
class DeviceService(
    private val deviceRepository: DeviceRepository
) {

    fun createDevice(
        request: CreateDeviceRequest
    ): Device {

        require(request.imei.isNotBlank()) {
            "IMEI must not be blank"
        }

        require(request.imei.length in 14..20) {
            "IMEI must contain between 14 and 20 digits"
        }

        require(request.imei.all { it.isDigit() }) {
            "IMEI must contain only digits"
        }

        if (deviceRepository.findByImei(request.imei) != null) {
            throw IllegalArgumentException(
                "Device with IMEI ${request.imei} already exists"
            )
        }

        val device = Device(
            id = 0,
            imei = request.imei,
            manufacturer = request.manufacturer,
            model = request.model,
            name = request.name,
            active = true
        )

        return deviceRepository.save(device)
    }

    fun listDevices(): List<Device> {
        return deviceRepository.findAll()
    }

    fun deleteDevice(imei: String): Boolean {

        require(imei.isNotBlank()) {
            "IMEI must not be blank"
        }

        return deviceRepository.deleteByImei(imei)
    }
}