package org.kisal.telematics.simulator

import java.nio.ByteBuffer
import java.nio.ByteOrder

object TeltonikaPacketBuilder {

    fun buildTestPacket(
        timestamp: Long,
        latitude: Double,
        longitude: Double,
        speed: Int,
        altitude: Int = 120,
        angle: Int = 90,
        satellites: Int = 10
    ): ByteArray {

        val longitudeValue =
            (longitude * 10_000_000).toInt()

        val latitudeValue =
            (latitude * 10_000_000).toInt()

        val gpsRecord =
            longBytes(timestamp) +
                    byteArrayOf(0x01) +
                    intBytes(longitudeValue) +
                    intBytes(latitudeValue) +
                    shortBytes(altitude) +
                    shortBytes(angle) +
                    byteArrayOf(satellites.toByte()) +
                    shortBytes(speed)

        val ioSection =
            byteArrayOf(
                0x01,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00
            )

        val record =
            gpsRecord +
                    ioSection +
                    byteArrayOf(0x01)

        val data =
            byteArrayOf(
                0x08,
                0x01
            ) + record

        val dataLength = data.size

        val crc = crc16(data)

        return byteArrayOf(
            0x00,
            0x00,
            0x00,
            0x00,

            ((dataLength shr 24) and 0xFF).toByte(),
            ((dataLength shr 16) and 0xFF).toByte(),
            ((dataLength shr 8) and 0xFF).toByte(),
            (dataLength and 0xFF).toByte()
        ) +
                data +
                byteArrayOf(
                    0x00,
                    0x00,
                    ((crc shr 8) and 0xFF).toByte(),
                    (crc and 0xFF).toByte()
                )
    }

    private fun longBytes(value: Long): ByteArray {
        return ByteBuffer
            .allocate(8)
            .order(ByteOrder.BIG_ENDIAN)
            .putLong(value)
            .array()
    }

    private fun intBytes(value: Int): ByteArray {
        return byteArrayOf(
            ((value shr 24) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            (value and 0xFF).toByte()
        )
    }

    private fun shortBytes(value: Int): ByteArray {
        return byteArrayOf(
            ((value shr 8) and 0xFF).toByte(),
            (value and 0xFF).toByte()
        )
    }

    private fun crc16(data: ByteArray): Int {

        var crc = 0x0000

        for (byte in data) {

            crc = crc xor (byte.toInt() and 0xFF)

            repeat(8) {

                crc =
                    if ((crc and 0x0001) != 0) {
                        (crc ushr 1) xor 0x8408
                    } else {
                        crc ushr 1
                    }
            }
        }

        return crc and 0xFFFF
    }
}