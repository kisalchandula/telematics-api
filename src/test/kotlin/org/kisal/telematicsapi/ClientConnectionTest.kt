package org.kisal.telematicsapi

import org.junit.jupiter.api.Test
import org.kisal.telematicsapi.device.DeviceSessionManager
import org.kisal.telematicsapi.domain.TelemetryEvent
import org.kisal.telematicsapi.domain.TelemetryRepository
import org.kisal.telematicsapi.protocol.AvlPacketDecoder
import org.kisal.telematicsapi.protocol.Crc16
import org.kisal.telematicsapi.protocol.TeltonikaDecoder
import org.kisal.telematicsapi.server.ClientConnection
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.test.assertEquals

class ClientConnectionTest {

    @Test
    fun `accept device and process IMEI`() {

        val serverSocket = ServerSocket(0)

        val port = serverSocket.localPort

        val sessionManager = DeviceSessionManager()

        val decoder = TeltonikaDecoder()

        val avlPacketDecoder = AvlPacketDecoder()

        val serverThread = thread {

            val socket = serverSocket.accept()

            ClientConnection(
                socket = socket,
                decoder = decoder,
                avlPacketDecoder = avlPacketDecoder,
                sessionManager = sessionManager
            ).handle()
        }

        val client = Socket(
            "localhost",
            port
        )

        val output = client.getOutputStream()
        val input = client.getInputStream()

        val imei = "123456789012345"

        val imeiBytes =
            imei.toByteArray(Charsets.US_ASCII)

        val imeiPacket =
            byteArrayOf(
                0x00,
                imeiBytes.size.toByte()
            ) + imeiBytes

        output.write(imeiPacket)
        output.flush()

        val response = input.read()

        assertEquals(
            0x01,
            response
        )

        Thread.sleep(100)

        assertEquals(
            1,
            sessionManager.count()
        )

        client.close()

        serverThread.join(1000)

        serverSocket.close()
    }

    @Test
    fun `accept device and acknowledge AVL packet`() {

        val serverSocket = ServerSocket(0)

        val port = serverSocket.localPort

        val sessionManager = DeviceSessionManager()

        val decoder = TeltonikaDecoder()

        val avlPacketDecoder = AvlPacketDecoder()

        val serverThread = thread {

            val socket = serverSocket.accept()

            ClientConnection(
                socket = socket,
                decoder = decoder,
                avlPacketDecoder = avlPacketDecoder,
                sessionManager = sessionManager
            ).handle()
        }

        val client = Socket(
            "localhost",
            port
        )

        val output = client.getOutputStream()
        val input = client.getInputStream()

        /*
         * Send IMEI
         */

        val imei = "123456789012345"

        val imeiBytes =
            imei.toByteArray(Charsets.US_ASCII)

        val imeiPacket =
            byteArrayOf(
                0x00,
                imeiBytes.size.toByte()
            ) + imeiBytes

        output.write(imeiPacket)
        output.flush()

        /*
         * Gateway should accept the device.
         */

        val imeiResponse = input.read()

        assertEquals(
            0x01,
            imeiResponse
        )

        /*
         * Send valid AVL packet.
         */

        val avlPacket = byteArrayOf(
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x03,
            0x08,
            0x01,
            0x01,
            0x00, 0x00, 0xCE.toByte(), 0x93.toByte()
        )

        output.write(avlPacket)
        output.flush()

        /*
         * AVL acknowledgement is 4 bytes.
         *
         * One record -> 0x00000001
         */

        val acknowledgement = ByteArray(4)

        var offset = 0

        while (offset < 4) {

            val bytesRead =
                input.read(
                    acknowledgement,
                    offset,
                    4 - offset
                )

            if (bytesRead == -1) {
                break
            }

            offset += bytesRead
        }

        assertEquals(
            4,
            offset
        )

        assertEquals(
            byteArrayOf(
                0x00,
                0x00,
                0x00,
                0x01
            ).toList(),
            acknowledgement.toList()
        )

        client.close()

        serverThread.join(1000)

        serverSocket.close()
    }

    @Test
    fun `process multiple AVL packets from one TCP read`() {

        val serverSocket = ServerSocket(0)

        val port = serverSocket.localPort

        val sessionManager = DeviceSessionManager()

        val decoder = TeltonikaDecoder()

        val avlPacketDecoder = AvlPacketDecoder()

        val serverThread = thread {

            val socket = serverSocket.accept()

            ClientConnection(
                socket = socket,
                decoder = decoder,
                avlPacketDecoder = avlPacketDecoder,
                sessionManager = sessionManager
            ).handle()
        }

        val client = Socket(
            "localhost",
            port
        )

        val output = client.getOutputStream()
        val input = client.getInputStream()

        /*
         * IMEI
         */

        val imei = "123456789012345"

        val imeiBytes =
            imei.toByteArray(Charsets.US_ASCII)

        val imeiPacket =
            byteArrayOf(
                0x00,
                imeiBytes.size.toByte()
            ) + imeiBytes

        output.write(imeiPacket)
        output.flush()

        assertEquals(
            0x01,
            input.read()
        )

        /*
         * Two valid AVL packets.
         */

        val avlPacket = byteArrayOf(
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x03,
            0x08,
            0x01,
            0x01,
            0x00, 0x00, 0xCE.toByte(), 0x93.toByte()
        )

        /*
         * Send both packets in one TCP write.
         */

        output.write(
            avlPacket + avlPacket
        )

        output.flush()

        /*
         * Read ACK #1 and ACK #2.
         */

        val acknowledgements =
            ByteArray(8)

        var offset = 0

        while (offset < 8) {

            val bytesRead =
                input.read(
                    acknowledgements,
                    offset,
                    8 - offset
                )

            if (bytesRead == -1) {
                break
            }

            offset += bytesRead
        }

        assertEquals(
            8,
            offset
        )

        /*
         * Each ACK should be 0x00000001.
         */

        val expected =
            byteArrayOf(
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x01
            )

        assertEquals(
            expected.toList(),
            acknowledgements.toList()
        )

        client.close()

        serverThread.join(1000)

        serverSocket.close()
    }

    @Test
    fun `process AVL packet into telemetry event`() {

        val serverSocket = ServerSocket(0)

        val port = serverSocket.localPort

        val sessionManager = DeviceSessionManager()

        val decoder = TeltonikaDecoder()

        val avlPacketDecoder = AvlPacketDecoder()

        val events = mutableListOf<TelemetryEvent>()

        val repository = object : TelemetryRepository {

            override fun save(event: TelemetryEvent) {
                events.add(event)
            }

            override fun findLatestByImei(imei: String): TelemetryEvent? {
                return events
                    .filter { it.imei == imei }
                    .maxByOrNull { it.timestamp }
            }

            override fun findByImei(imei: String): List<TelemetryEvent> {
                return events
                    .filter { it.imei == imei }
                    .sortedByDescending { it.timestamp }
            }
        }

        val serverThread = thread {

            val socket = serverSocket.accept()

            ClientConnection(
                socket = socket,
                decoder = decoder,
                avlPacketDecoder = avlPacketDecoder,
                sessionManager = sessionManager,
                telemetryEventHandler = { event ->
                    repository.save(event)
                }
            ).handle()
        }

        val client = Socket(
            "localhost",
            port
        )

        /*
         * Prevent the test from hanging forever
         * if the server does not send a response.
         */

        client.soTimeout = 3000

        val output = client.getOutputStream()
        val input = client.getInputStream()

        /*
         * Send IMEI.
         */

        val imei = "123456789012345"

        val imeiBytes =
            imei.toByteArray(Charsets.US_ASCII)

        val imeiPacket =
            byteArrayOf(
                0x00,
                imeiBytes.size.toByte()
            ) + imeiBytes

        output.write(imeiPacket)
        output.flush()

        /*
         * Gateway should accept the device.
         */

        val imeiResponse = input.read()

        assertEquals(
            0x01,
            imeiResponse
        )

        /*
         * Build one Codec 8 GPS record.
         *
         * Timestamp:
         * 2026-10-02T10:00:00Z
         */

        val timestamp = 1790935200000L

        val timestampBytes =
            ByteArray(8) { index ->
                (timestamp shr (56 - index * 8)).toByte()
            }

        /*
         * GPS values:
         *
         * Longitude = 8.4037
         * Latitude  = 49.0069
         * Altitude  = 120
         * Angle     = 90
         * Satellites = 10
         * Speed     = 50
         */

        val longitude =
            (8.4037 * 10_000_000).toInt()

        val latitude =
            (49.0069 * 10_000_000).toInt()

        /*
         * GPS section.
         *
         * Timestamp     8 bytes
         * Priority      1 byte
         * Longitude     4 bytes
         * Latitude      4 bytes
         * Altitude      2 bytes
         * Angle         2 bytes
         * Satellites    1 byte
         * Speed         2 bytes
         *
         * Total = 24 bytes
         */

        val gpsRecord =
            timestampBytes +
                    byteArrayOf(
                        0x01
                    ) +
                    intBytes(longitude) +
                    intBytes(latitude) +
                    shortBytes(120) +
                    shortBytes(90) +
                    byteArrayOf(
                        10
                    ) +
                    shortBytes(50)

        assertEquals(
            24,
            gpsRecord.size
        )

        /*
         * Codec 8 IO section.
         *
         * Event ID       = 1
         * Total IO       = 0
         * 1-byte IO      = 0
         * 2-byte IO      = 0
         * 4-byte IO      = 0
         * 8-byte IO      = 0
         *
         * Total = 6 bytes
         */

        val ioSection =
            byteArrayOf(
                0x01,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00
            )

        /*
         * Codec 8 has a second record-count byte
         * after all records.
         *
         * One record -> 0x01
         */

        val record =
            gpsRecord +
                    ioSection +
                    byteArrayOf(
                        0x01
                    )

        /*
         * GPS       = 24
         * IO        = 6
         * Count     = 1
         * ----------------
         * Record    = 31
         */

        assertEquals(
            31,
            record.size
        )

        /*
         * Codec 8 data:
         *
         * Codec ID       = 0x08
         * Number records = 0x01
         * Record         = 31 bytes
         *
         * Total = 33 bytes
         */

        val data =
            byteArrayOf(
                0x08,
                0x01
            ) + record

        val dataLength =
            data.size

        assertEquals(
            33,
            dataLength
        )

        /*
         * Calculate CRC over AVL data.
         */

        val crc =
            Crc16.calculate(data)

        /*
         * Build complete Teltonika AVL packet.
         *
         * Preamble       4 bytes
         * Data length    4 bytes
         * Data          33 bytes
         * CRC             4 bytes
         */

        val avlPacket =
            byteArrayOf(
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

        /*
         * Send AVL packet.
         */

        output.write(avlPacket)
        output.flush()

        /*
         * Read AVL acknowledgement.
         */

        val acknowledgement =
            ByteArray(4)

        var offset = 0

        while (offset < 4) {

            val bytesRead =
                input.read(
                    acknowledgement,
                    offset,
                    4 - offset
                )

            if (bytesRead == -1) {
                break
            }

            offset += bytesRead
        }

        assertEquals(
            4,
            offset
        )

        /*
         * One AVL record -> 0x00000001.
         */

        assertEquals(
            byteArrayOf(
                0x00,
                0x00,
                0x00,
                0x01
            ).toList(),
            acknowledgement.toList()
        )

        /*
         * Verify that the complete TCP -> domain
         * pipeline produced one TelemetryEvent.
         */

        assertEquals(
            1,
            events.size
        )

        val event =
            events.first()

        assertEquals(
            imei,
            event.imei
        )

        assertEquals(
            49.0069,
            event.latitude,
            0.000001
        )

        assertEquals(
            8.4037,
            event.longitude,
            0.000001
        )

        assertEquals(
            120,
            event.altitude
        )

        assertEquals(
            90,
            event.angle
        )

        assertEquals(
            10,
            event.satellites
        )

        assertEquals(
            50,
            event.speed
        )

        /*
         * Cleanup.
         */

        client.close()

        serverThread.join(1000)

        serverSocket.close()
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
}