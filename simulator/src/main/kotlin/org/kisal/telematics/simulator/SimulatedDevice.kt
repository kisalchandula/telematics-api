package org.kisal.telematics.simulator

import java.net.Socket

class SimulatedDevice(
    private val host: String,
    private val port: Int,
    private val imei: String,
    private val routeFile: String
) {

    private val route = loadRoute()

    private fun loadRoute(): List<GpsPoint> {

        val inputStream = Thread
            .currentThread()
            .contextClassLoader
            .getResourceAsStream("routes/$routeFile")

        requireNotNull(inputStream) {
            "Could not find routes/$routeFile"
        }

        return inputStream.use {
            GpxRoute.load(it)
        }
    }

    private fun calculateHeading(
        current: GpsPoint,
        next: GpsPoint
    ): Double {

        val lat1 =
            Math.toRadians(current.latitude)

        val lat2 =
            Math.toRadians(next.latitude)

        val deltaLongitude =
            Math.toRadians(
                next.longitude - current.longitude
            )

        val y =
            kotlin.math.sin(deltaLongitude) *
                    kotlin.math.cos(lat2)

        val x =
            kotlin.math.cos(lat1) *
                    kotlin.math.sin(lat2) -
                    kotlin.math.sin(lat1) *
                    kotlin.math.cos(lat2) *
                    kotlin.math.cos(deltaLongitude)

        val heading =
            Math.toDegrees(
                kotlin.math.atan2(y, x)
            )

        return (heading + 360.0) % 360.0
    }

    private fun calculateDistanceMeters(
        first: GpsPoint,
        second: GpsPoint
    ): Double {

        val earthRadius = 6_371_000.0

        val lat1 =
            Math.toRadians(first.latitude)

        val lat2 =
            Math.toRadians(second.latitude)

        val deltaLat =
            Math.toRadians(
                second.latitude - first.latitude
            )

        val deltaLon =
            Math.toRadians(
                second.longitude - first.longitude
            )

        val a =
            kotlin.math.sin(deltaLat / 2) *
                    kotlin.math.sin(deltaLat / 2) +
                    kotlin.math.cos(lat1) *
                    kotlin.math.cos(lat2) *
                    kotlin.math.sin(deltaLon / 2) *
                    kotlin.math.sin(deltaLon / 2)

        val c =
            2 * kotlin.math.atan2(
                kotlin.math.sqrt(a),
                kotlin.math.sqrt(1 - a)
            )

        return earthRadius * c
    }

    fun connect() {

        Socket(host, port).use { socket ->

            println(
                "Connected to gateway $host:$port"
            )

            val input =
                socket.getInputStream()

            val output =
                socket.getOutputStream()

            // -------------------------
            // IMEI handshake
            // -------------------------

            val imeiBytes =
                imei.toByteArray(Charsets.UTF_8)

            val imeiPacket =
                ByteArray(2 + imeiBytes.size)

            imeiPacket[0] =
                ((imeiBytes.size shr 8) and 0xFF)
                    .toByte()

            imeiPacket[1] =
                (imeiBytes.size and 0xFF)
                    .toByte()

            System.arraycopy(
                imeiBytes,
                0,
                imeiPacket,
                2,
                imeiBytes.size
            )

            output.write(imeiPacket)
            output.flush()

            println(
                "IMEI sent: $imei"
            )

            val imeiAck =
                ByteArray(1)

            input.read(imeiAck)

            println(
                "IMEI ACK received: " +
                        imeiAck[0].toInt()
            )

            // -------------------------
            // Continuous route
            // -------------------------

            var index = 0

            while (true) {

                val point =
                    route[index]

                val nextPoint =
                    route[
                        (index + 1) % route.size
                    ]

                val latitude =
                    point.latitude

                val longitude =
                    point.longitude

                val heading =
                    calculateHeading(
                        point,
                        nextPoint
                    )

                val packet =
                    TeltonikaPacketBuilder.buildTestPacket(
                        timestamp =
                            System.currentTimeMillis(),
                        latitude =
                            latitude,
                        longitude =
                            longitude,
                        speed = 40,
                        altitude =
                            point.elevation?.toInt()
                                ?: 120,
                        angle =
                            heading.toInt(),
                        satellites = 10
                    )

                output.write(packet)
                output.flush()

                println(
                    "Telemetry sent: " +
                            "lat=$latitude " +
                            "lon=$longitude " +
                            "heading=${heading.toInt()}"
                )

                val ack =
                    ByteArray(4)

                input.read(ack)

                println(
                    "AVL ACK received"
                )

                val distanceMeters =
                    calculateDistanceMeters(
                        point,
                        nextPoint
                    )

                val speedMetersPerSecond =
                    40.0 / 3.6

                val delayMillis =
                    (
                            distanceMeters /
                                    speedMetersPerSecond *
                                    1000
                            ).toLong()

                Thread.sleep(delayMillis)

                index++

                if (index >= route.size) {
                    index = 0
                }
            }
        }
    }
}