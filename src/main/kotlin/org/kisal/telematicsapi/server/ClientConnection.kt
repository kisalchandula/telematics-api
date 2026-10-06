package org.kisal.telematicsapi.server

import device.DeviceSession
import device.DeviceSessionManager
import org.kisal.telematicsapi.domain.TelemetryEvent
import org.kisal.telematicsapi.domain.TelemetryEventMapper
import protocol.AvlPacketDecoder
import protocol.Codec8Decoder
import protocol.PacketBuffer
import protocol.ProtocolDecoder
import protocol.ProtocolMessage
import java.net.Socket

class ClientConnection(
    private val socket: Socket,
    private val decoder: ProtocolDecoder,
    private val avlPacketDecoder: AvlPacketDecoder,
    private val sessionManager: DeviceSessionManager,
    private val codec8Decoder: Codec8Decoder = Codec8Decoder(),
    private val telemetryEventHandler: (TelemetryEvent) -> Unit = {}
) {

    private var state = ConnectionState.WAITING_FOR_IMEI

    private fun processAvlPackets(
        receiveBuffer: PacketBuffer,
        session: DeviceSession?
    ) {

        while (true) {

            if (receiveBuffer.size() < 8) {
                return
            }

            val dataLength =
                ((receiveBuffer.peek(4).toInt() and 0xFF) shl 24) or
                        ((receiveBuffer.peek(5).toInt() and 0xFF) shl 16) or
                        ((receiveBuffer.peek(6).toInt() and 0xFF) shl 8) or
                        (receiveBuffer.peek(7).toInt() and 0xFF)

            val packetLength =
                8 + dataLength + 4

            if (receiveBuffer.size() < packetLength) {
                return
            }

            val packet =
                receiveBuffer.read(packetLength)

            val avlPacket =
                avlPacketDecoder.decode(packet)

            if (avlPacket == null) {

                println(
                    "Invalid AVL packet from device ${session?.imei}"
                )

                return
            }

            println(
                "AVL packet received from device ${session?.imei}"
            )

            println(
                "Codec ID: ${avlPacket.codecId}"
            )

            println(
                "Records: ${avlPacket.recordCount}"
            )

            val records =
                codec8Decoder.decode(avlPacket)

            for (record in records) {

                val imei = session?.imei ?: continue

                val telemetryEvent =
                    TelemetryEventMapper.map(
                        imei = imei,
                        record = record
                    )

                telemetryEventHandler(telemetryEvent)

                println(
                    "Telemetry event: " +
                            "${telemetryEvent.latitude}, " +
                            "${telemetryEvent.longitude}, " +
                            "${telemetryEvent.speed} km/h"
                )
            }

            sendAvlAcknowledgement(
                avlPacket.recordCount
            )
        }
    }

    private fun sendAvlAcknowledgement(
        recordCount: Int
    ) {

        socket.getOutputStream().write(
            byteArrayOf(
                ((recordCount shr 24) and 0xFF).toByte(),
                ((recordCount shr 16) and 0xFF).toByte(),
                ((recordCount shr 8) and 0xFF).toByte(),
                (recordCount and 0xFF).toByte()
            )
        )

        socket.getOutputStream().flush()

        println(
            "AVL acknowledgement sent: $recordCount"
        )
    }

    fun handle() {

        println(
            "Client connected: " +
                    "${socket.inetAddress.hostAddress}:${socket.port}"
        )

        val receiveBuffer = PacketBuffer()

        var session: DeviceSession? = null

        socket.use {

            val input = it.getInputStream()
            val buffer = ByteArray(1024)

            while (true) {

                val bytesRead = input.read(buffer)

                if (bytesRead == -1) {
                    break
                }

                val data = buffer.copyOf(bytesRead)

                receiveBuffer.append(data)

                when (state) {

                    ConnectionState.WAITING_FOR_IMEI -> {

                        val newSession =
                            processImei(receiveBuffer)

                        if (newSession != null) {

                            session = newSession

                            state = ConnectionState.CONNECTED

                            println(
                                "Connection state: $state"
                            )

                            processAvlPackets(
                                receiveBuffer,
                                session
                            )
                        }
                    }

                    ConnectionState.CONNECTED -> {

                        println(
                            "Received ${data.size} bytes from " +
                                    "device ${session?.imei}"
                        )

                        processAvlPackets(
                            receiveBuffer,
                            session
                        )
                    }
                }

                session?.updateLastSeen()
            }
        }

        session?.let {
            sessionManager.remove(it.imei)

            println(
                "Device removed: ${it.imei}"
            )

            println(
                "Active devices: ${sessionManager.count()}"
            )
        }

        println("Client disconnected")
    }

    private fun processImei(
        receiveBuffer: PacketBuffer
    ): DeviceSession? {

        /*
         * Teltonika IMEI packet:
         *
         * 2 bytes -> IMEI length
         * N bytes -> IMEI
         */

        if (receiveBuffer.size() < 2) {
            return null
        }

        val imeiLength =
            ((receiveBuffer.peek(0).toInt() and 0xFF) shl 8) or
                    (receiveBuffer.peek(1).toInt() and 0xFF)

        val packetLength = 2 + imeiLength

        if (receiveBuffer.size() < packetLength) {
            return null
        }

        val packet = receiveBuffer.read(packetLength)

        val message = decoder.decode(packet)

        if (message !is ProtocolMessage.Imei) {
            return null
        }

        val newSession = DeviceSession(
            imei = message.value,
            socket = socket
        )

        sessionManager.register(newSession)

        println(
            "Device connected: ${newSession.imei}"
        )

        println(
            "Active devices: ${sessionManager.count()}"
        )

        acceptDevice()

        return newSession
    }

    private fun acceptDevice() {

        socket.getOutputStream().write(
            byteArrayOf(0x01)
        )

        socket.getOutputStream().flush()

        println("Device accepted")
    }
}