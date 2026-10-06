package org.kisal.telematicsapi

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

import org.kisal.telematicsapi.protocol.AvlPacket
import org.kisal.telematicsapi.protocol.Codec8Decoder
import kotlin.test.assertEquals

class Codec8DecoderTest {

    private val decoder = Codec8Decoder()

    @Test
    fun `decode GPS data from Codec 8 record`() {

        val packet = AvlPacket(
            codecId = 0x08,
            recordCount = 1,
            data = byteArrayOf(
                // Codec ID
                0x08,

                // Record count
                0x01,

                // Timestamp: 1,700,000,000,000
                0x00,
                0x00,
                0x01,
                0x8B.toByte(),
                0xCF.toByte(),
                0xE5.toByte(),
                0x68,
                0x00,

                // Priority
                0x01,

                // Longitude: 8.0000000°
                0x04,
                0xC4.toByte(),
                0xB4.toByte(),
                0x00,

                // Latitude: 49.0000000°
                0x1D,
                0x34,
                0xCE.toByte(),
                0x80.toByte(),

                // Altitude: 120 m
                0x00,
                0x78,

                // Angle: 90°
                0x00,
                0x5A,

                // Satellites: 10
                0x0A,

                // Speed: 50 km/h
                0x00,
                0x32,

                // IO Event ID
                0x01,

                // Total IO elements
                0x00,

                // 1-byte IO count
                0x00,

                // 2-byte IO count
                0x00,

                // 4-byte IO count
                0x00,

                // 8-byte IO count
                0x00,

                // Second record count
                0x01
            ),
            crc = 0
        )

        val result = decoder.decode(packet)

        assertEquals(1, result.size)

        val record = result[0]

        assertNotNull(record)

        assertEquals(
            1700000000000L,
            record.gps.timestamp
        )

        assertEquals(
            1,
            record.gps.priority
        )

        assertEquals(
            8.0,
            record.gps.longitude,
            0.0000001
        )

        assertEquals(
            49.0,
            record.gps.latitude,
            0.0000001
        )

        assertEquals(
            120,
            record.gps.altitude
        )

        assertEquals(
            90,
            record.gps.angle
        )

        assertEquals(
            10,
            record.gps.satellites
        )

        assertEquals(
            50,
            record.gps.speed
        )
    }

    @Test
    fun `decode one byte IO element`() {

        val packet = AvlPacket(
            codecId = 0x08,
            recordCount = 1,
            data = byteArrayOf(
                // Codec ID
                0x08,

                // Record count
                0x01,

                // Timestamp
                0x00,
                0x00,
                0x01,
                0x8B.toByte(),
                0xCF.toByte(),
                0xE5.toByte(),
                0x68,
                0x00,

                // Priority
                0x01,

                // Longitude: 8.0
                0x04,
                0xC4.toByte(),
                0xB4.toByte(),
                0x00,

                // Latitude: 49.0
                0x1D,
                0x34,
                0xCE.toByte(),
                0x80.toByte(),

                // Altitude
                0x00,
                0x78,

                // Angle
                0x00,
                0x5A,

                // Satellites
                0x0A,

                // Speed
                0x00,
                0x32,

                // Event ID
                0x01,

                // Total IO
                0x01,

                // 1-byte IO count
                0x01,

                // IO ID = 66
                0x42,

                // IO value = 1
                0x01,

                // 2-byte IO count
                0x00,

                // 4-byte IO count
                0x00,

                // 8-byte IO count
                0x00,

                // Second record count
                0x01
            ),
            crc = 0
        )

        val result = decoder.decode(packet)

        assertEquals(1, result.size)

        val record = result[0]

        assertEquals(1, record.eventId)

        assertEquals(1, record.ioElements.size)

        val io = record.ioElements[0]

        assertEquals(66, io.id)

        assertEquals(1L, io.value)
    }

    @Test
    fun `decode two byte IO element`() {

        val packet = AvlPacket(
            codecId = 0x08,
            recordCount = 1,
            data = byteArrayOf(
                // Codec ID
                0x08,

                // Record count
                0x01,

                // Timestamp
                0x00,
                0x00,
                0x01,
                0x8B.toByte(),
                0xCF.toByte(),
                0xE5.toByte(),
                0x68,
                0x00,

                // Priority
                0x01,

                // Longitude: 8.0
                0x04,
                0xC4.toByte(),
                0xB4.toByte(),
                0x00,

                // Latitude: 49.0
                0x1D,
                0x34,
                0xCE.toByte(),
                0x80.toByte(),

                // Altitude
                0x00,
                0x78,

                // Angle
                0x00,
                0x5A,

                // Satellites
                0x0A,

                // Speed
                0x00,
                0x32,

                // Event ID
                0x01,

                // Total IO = 1
                0x01,

                // 1-byte IO count
                0x00,

                // 2-byte IO count
                0x01,

                // IO ID = 67
                0x43,

                // IO value = 500
                0x01,
                0xF4.toByte(),

                // 4-byte IO count
                0x00,

                // 8-byte IO count
                0x00,

                // Second record count
                0x01
            ),
            crc = 0
        )

        val result = decoder.decode(packet)

        assertEquals(1, result.size)

        val record = result[0]

        assertEquals(1, record.eventId)

        assertEquals(1, record.ioElements.size)

        val io = record.ioElements[0]

        assertEquals(67, io.id)

        assertEquals(500L, io.value)
    }

    @Test
    fun `decode four byte IO element`() {

        val packet = AvlPacket(
            codecId = 0x08,
            recordCount = 1,
            data = byteArrayOf(
                // Codec ID
                0x08,

                // Record count
                0x01,

                // Timestamp
                0x00,
                0x00,
                0x01,
                0x8B.toByte(),
                0xCF.toByte(),
                0xE5.toByte(),
                0x68,
                0x00,

                // Priority
                0x01,

                // Longitude: 8.0
                0x04,
                0xC4.toByte(),
                0xB4.toByte(),
                0x00,

                // Latitude: 49.0
                0x1D,
                0x34,
                0xCE.toByte(),
                0x80.toByte(),

                // Altitude
                0x00,
                0x78,

                // Angle
                0x00,
                0x5A,

                // Satellites
                0x0A,

                // Speed
                0x00,
                0x32,

                // Event ID
                0x01,

                // Total IO = 1
                0x01,

                // 1-byte IO count
                0x00,

                // 2-byte IO count
                0x00,

                // 4-byte IO count
                0x01,

                // IO ID = 68
                0x44,

                // IO value = 100000
                0x00,
                0x01,
                0x86.toByte(),
                0xA0.toByte(),

                // 8-byte IO count
                0x00,

                // Second record count
                0x01
            ),
            crc = 0
        )

        val result = decoder.decode(packet)

        assertEquals(1, result.size)

        val record = result[0]

        assertEquals(1, record.eventId)

        assertEquals(1, record.ioElements.size)

        val io = record.ioElements[0]

        assertEquals(68, io.id)

        assertEquals(100000L, io.value)
    }

    @Test
    fun `decode eight byte IO element`() {

        val packet = AvlPacket(
            codecId = 0x08,
            recordCount = 1,
            data = byteArrayOf(
                // Codec ID
                0x08,

                // Record count
                0x01,

                // Timestamp
                0x00,
                0x00,
                0x01,
                0x8B.toByte(),
                0xCF.toByte(),
                0xE5.toByte(),
                0x68,
                0x00,

                // Priority
                0x01,

                // Longitude: 8.0
                0x04,
                0xC4.toByte(),
                0xB4.toByte(),
                0x00,

                // Latitude: 49.0
                0x1D,
                0x34,
                0xCE.toByte(),
                0x80.toByte(),

                // Altitude
                0x00,
                0x78,

                // Angle
                0x00,
                0x5A,

                // Satellites
                0x0A,

                // Speed
                0x00,
                0x32,

                // Event ID
                0x01,

                // Total IO = 1
                0x01,

                // 1-byte IO count
                0x00,

                // 2-byte IO count
                0x00,

                // 4-byte IO count
                0x00,

                // 8-byte IO count
                0x01,

                // IO ID = 69
                0x45,

                // IO value = 123456789
                0x00,
                0x00,
                0x00,
                0x00,
                0x07,
                0x5B,
                0xCD.toByte(),
                0x15,

                // Second record count
                0x01
            ),
            crc = 0
        )

        val result = decoder.decode(packet)

        assertEquals(1, result.size)

        val record = result[0]

        assertEquals(1, record.eventId)

        assertEquals(1, record.ioElements.size)

        val io = record.ioElements[0]

        assertEquals(69, io.id)

        assertEquals(123456789L, io.value)
    }

    @Test
    fun `decode multiple IO elements`() {

        val packet = AvlPacket(
            codecId = 0x08,
            recordCount = 1,
            data = byteArrayOf(
                // Codec ID
                0x08,

                // Record count
                0x01,

                // Timestamp
                0x00,
                0x00,
                0x01,
                0x8B.toByte(),
                0xCF.toByte(),
                0xE5.toByte(),
                0x68,
                0x00,

                // Priority
                0x01,

                // Longitude: 8.0
                0x04,
                0xC4.toByte(),
                0xB4.toByte(),
                0x00,

                // Latitude: 49.0
                0x1D,
                0x34,
                0xCE.toByte(),
                0x80.toByte(),

                // Altitude
                0x00,
                0x78,

                // Angle
                0x00,
                0x5A,

                // Satellites
                0x0A,

                // Speed
                0x00,
                0x32,

                // Event ID
                0x01,

                // Total IO = 4
                0x04,

                // -------------------------
                // 1-byte IO
                // -------------------------

                // Count = 1
                0x01,

                // ID = 66
                0x42,

                // Value = 1
                0x01,

                // -------------------------
                // 2-byte IO
                // -------------------------

                // Count = 1
                0x01,

                // ID = 67
                0x43,

                // Value = 500
                0x01,
                0xF4.toByte(),

                // -------------------------
                // 4-byte IO
                // -------------------------

                // Count = 1
                0x01,

                // ID = 68
                0x44,

                // Value = 100000
                0x00,
                0x01,
                0x86.toByte(),
                0xA0.toByte(),

                // -------------------------
                // 8-byte IO
                // -------------------------

                // Count = 1
                0x01,

                // ID = 69
                0x45,

                // Value = 123456789
                0x00,
                0x00,
                0x00,
                0x00,
                0x07,
                0x5B,
                0xCD.toByte(),
                0x15,

                // Second record count
                0x01
            ),
            crc = 0
        )

        val result = decoder.decode(packet)

        assertEquals(1, result.size)

        val record = result[0]

        assertEquals(1, record.eventId)

        assertEquals(4, record.ioElements.size)

        assertEquals(66, record.ioElements[0].id)
        assertEquals(1L, record.ioElements[0].value)

        assertEquals(67, record.ioElements[1].id)
        assertEquals(500L, record.ioElements[1].value)

        assertEquals(68, record.ioElements[2].id)
        assertEquals(100000L, record.ioElements[2].value)

        assertEquals(69, record.ioElements[3].id)
        assertEquals(123456789L, record.ioElements[3].value)
    }

    @Test
    fun `reject mismatched IO count`() {

        val packet = AvlPacket(
            codecId = 0x08,
            recordCount = 1,
            data = byteArrayOf(
                // Codec ID
                0x08,

                // Record count
                0x01,

                // Timestamp
                0x00,
                0x00,
                0x01,
                0x8B.toByte(),
                0xCF.toByte(),
                0xE5.toByte(),
                0x68,
                0x00,

                // Priority
                0x01,

                // Longitude
                0x04,
                0xC4.toByte(),
                0xB4.toByte(),
                0x00,

                // Latitude
                0x1D,
                0x34,
                0xCE.toByte(),
                0x80.toByte(),

                // Altitude
                0x00,
                0x78,

                // Angle
                0x00,
                0x5A,

                // Satellites
                0x0A,

                // Speed
                0x00,
                0x32,

                // Event ID
                0x01,

                // Total IO = 2
                0x02,

                // 1-byte IO count = 1
                0x01,

                // IO ID
                0x42,

                // IO value
                0x01,

                // 2-byte IO count
                0x00,

                // 4-byte IO count
                0x00,

                // 8-byte IO count
                0x00,

                // Second record count
                0x01
            ),
            crc = 0
        )

        val result = decoder.decode(packet)

        assertEquals(
            emptyList(),
            result
        )
    }

    private fun intBytes(value: Int): ByteArray =
        byteArrayOf(
            (value shr 24).toByte(),
            (value shr 16).toByte(),
            (value shr 8).toByte(),
            value.toByte()
        )

    private fun shortBytes(value: Int): ByteArray =
        byteArrayOf(
            (value shr 8).toByte(),
            value.toByte()
        )

    private fun longBytes(value: Long): ByteArray =
        ByteArray(8) { index ->
            (value shr (56 - index * 8)).toByte()
        }

    @Test
    fun `decode multiple AVL records`() {

        fun createRecord(
            timestamp: Long,
            priority: Int,
            longitude: Double,
            latitude: Double,
            altitude: Int,
            angle: Int,
            satellites: Int,
            speed: Int,
            eventId: Int,
            ioId: Int,
            ioValue: Int
        ): ByteArray {

            val gps =
                longBytes(timestamp) +
                        byteArrayOf(priority.toByte()) +
                        intBytes((longitude * 10_000_000).toInt()) +
                        intBytes((latitude * 10_000_000).toInt()) +
                        shortBytes(altitude) +
                        shortBytes(angle) +
                        byteArrayOf(satellites.toByte()) +
                        shortBytes(speed)

            val io =
                byteArrayOf(
                    eventId.toByte(),
                    0x01,

                    // 1-byte IO count
                    0x01,
                    ioId.toByte(),
                    ioValue.toByte(),

                    // 2-byte IO count
                    0x00,

                    // 4-byte IO count
                    0x00,

                    // 8-byte IO count
                    0x00
                )

            return gps + io
        }

        val record1 =
            createRecord(
                timestamp = 1_700_000_000_000L,
                priority = 1,
                longitude = 8.0,
                latitude = 49.0,
                altitude = 120,
                angle = 90,
                satellites = 10,
                speed = 50,
                eventId = 1,
                ioId = 66,
                ioValue = 1
            )

        val record2 =
            createRecord(
                timestamp = 1_700_000_010_000L,
                priority = 2,
                longitude = 8.1,
                latitude = 49.1,
                altitude = 125,
                angle = 95,
                satellites = 11,
                speed = 60,
                eventId = 2,
                ioId = 67,
                ioValue = 2
            )

        val data =
            byteArrayOf(
                // Codec ID
                0x08,

                // Record count
                0x02
            ) +
                    record1 +
                    record2 +
                    byteArrayOf(
                        // Second record count
                        0x02
                    )

        val packet =
            AvlPacket(
                codecId = 0x08,
                recordCount = 2,
                data = data,
                crc = 0
            )

        val result =
            decoder.decode(packet)

        assertEquals(2, result.size)

        val first = result[0]

        assertEquals(
            1_700_000_000_000L,
            first.gps.timestamp
        )

        assertEquals(
            8.0,
            first.gps.longitude,
            0.0000001
        )

        assertEquals(
            49.0,
            first.gps.latitude,
            0.0000001
        )

        assertEquals(1, first.eventId)
        assertEquals(1, first.ioElements.size)
        assertEquals(66, first.ioElements[0].id)
        assertEquals(1L, first.ioElements[0].value)

        val second = result[1]

        assertEquals(
            1_700_000_010_000L,
            second.gps.timestamp
        )

        assertEquals(
            8.1,
            second.gps.longitude,
            0.0000001
        )

        assertEquals(
            49.1,
            second.gps.latitude,
            0.0000001
        )

        assertEquals(2, second.eventId)
        assertEquals(1, second.ioElements.size)
        assertEquals(67, second.ioElements[0].id)
        assertEquals(2L, second.ioElements[0].value)
    }
}