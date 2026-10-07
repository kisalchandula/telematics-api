package org.kisal.telematics.simulator

fun main() {

    println("TELEMATICS DEVICE SIMULATOR")

    val device = SimulatedDevice(
        host = "127.0.0.1",
        port = 5000,
        imei = "356307042441013"
    )

    device.connect()
}