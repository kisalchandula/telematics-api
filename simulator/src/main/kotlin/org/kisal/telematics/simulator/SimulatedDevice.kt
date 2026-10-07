package org.kisal.telematics.simulator

import java.net.Socket

class SimulatedDevice(
    private val host: String,
    private val port: Int,
    private val imei: String
) {

    private val route = listOf(
        Pair(49.0069, 8.4037),
        Pair(49.0072, 8.4041),
        Pair(49.0076, 8.4048),
        Pair(49.0080, 8.4055),
        Pair(49.0084, 8.4062),
        Pair(49.0088, 8.4069),
        Pair(49.0092, 8.4076),
        Pair(49.0096, 8.4083),
        Pair(49.0100, 8.4090),
        Pair(49.0104, 8.4097)
    )

    fun connect() {

        Socket(host, port).use { socket ->

            println("Connected to gateway $host:$port")

            val input = socket.getInputStream()
            val output = socket.getOutputStream()

            // -------------------------
            // IMEI handshake
            // -------------------------

            val imeiBytes = imei.toByteArray(Charsets.UTF_8)

            val imeiPacket = ByteArray(2 + imeiBytes.size)

            imeiPacket[0] =
                ((imeiBytes.size shr 8) and 0xFF).toByte()

            imeiPacket[1] =
                (imeiBytes.size and 0xFF).toByte()

            System.arraycopy(
                imeiBytes,
                0,
                imeiPacket,
                2,
                imeiBytes.size
            )

            output.write(imeiPacket)
            output.flush()

            println("IMEI sent: $imei")

            // Wait for IMEI ACK
            val imeiAck = ByteArray(1)
            input.read(imeiAck)

            println(
                "IMEI ACK received: ${imeiAck[0].toInt()}"
            )

            // -------------------------
            // Continuous route
            // -------------------------

            var index = 0

            while (true) {

                val point = route[index]

                val latitude = point.first
                val longitude = point.second

                val packet = TeltonikaPacketBuilder.buildTestPacket(
                    timestamp = System.currentTimeMillis(),
                    latitude = latitude,
                    longitude = longitude,
                    speed = 40,
                    altitude = 120,
                    angle = 90,
                    satellites = 10
                )

                output.write(packet)
                output.flush()

                println(
                    "Telemetry sent: " +
                            "lat=$latitude " +
                            "lon=$longitude"
                )

                // Wait for AVL ACK
                val ack = ByteArray(4)
                input.read(ack)

                println(
                    "AVL ACK received"
                )

                Thread.sleep(2000)

                index++

                if (index >= route.size) {
                    index = 0
                }
            }
        }
    }
}

