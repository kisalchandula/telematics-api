package org.kisal.telematicsapi.domain

interface DeviceRepository {

    fun findByImei(imei: String): Device?

    fun findAll(): List<Device>

    fun save(device: Device): Device

    fun deleteByImei(imei: String): Boolean
}