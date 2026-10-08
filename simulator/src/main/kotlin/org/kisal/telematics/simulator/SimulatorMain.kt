package org.kisal.telematics.simulator

fun main() {

    println("TELEMATICS DEVICE SIMULATOR")

    val devices = listOf(

        SimulatedDevice(
            host = "127.0.0.1",
            port = 5000,
            imei = "356307042441013",
            routeFile = "karlsruhe-route.gpx"
        ),

        SimulatedDevice(
            host = "127.0.0.1",
            port = 5000,
            imei = "356307042441014",
            routeFile = "karlsruhe-route-2.gpx.gpx"
        ),

        SimulatedDevice(
            host = "127.0.0.1",
            port = 5000,
            imei = "356307042441015",
            routeFile = "karlsruhe-route-3.gpx.gpx"
        )
    )

    devices.forEach { device ->

        Thread {
            device.connect()
        }.start()

    }
}