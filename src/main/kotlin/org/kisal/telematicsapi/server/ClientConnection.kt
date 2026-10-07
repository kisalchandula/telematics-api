package org.kisal.telematicsapi.server

import org.kisal.telematicsapi.domain.TelemetryRepository
import org.kisal.telematicsapi.device.DeviceSession
import org.kisal.telematicsapi.device.DeviceSessionManager
import org.kisal.telematicsapi.domain.DeviceRepository
import org.kisal.telematicsapi.domain.TelemetryEvent
import org.kisal.telematicsapi.domain.TelemetryEventMapper
import org.kisal.telematicsapi.protocol.AvlPacketDecoder
import org.kisal.telematicsapi.protocol.Codec8Decoder
import org.kisal.telematicsapi.protocol.PacketBuffer
import org.kisal.telematicsapi.protocol.ProtocolDecoder
import org.kisal.telematicsapi.protocol.ProtocolMessage
import java.net.Socket

class ClientConnection(
    private val socket: Socket,
    private val decoder: ProtocolDecoder,
    private val avlPacketDecoder: AvlPacketDecoder,
    private val sessionManager: DeviceSessionManager,
    private val deviceRepository: DeviceRepository,
    private val telemetryRepository: TelemetryRepository,
    private val codec8Decoder: Codec8Decoder = Codec8Decoder(),
    private val telemetryEventHandler: (TelemetryEvent) -> Unit = {}
) {

    private var state = ConnectionState.WAITING_FOR_IMEI

    private var deviceId: Long? = null

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

            val packetLength = 8 + dataLength + 4

            if (receiveBuffer.size() < packetLength) {
                return
            }

            val packet = receiveBuffer.read(packetLength)

            val avlPacket = avlPacketDecoder.decode(packet)

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

            val records = codec8Decoder.decode(avlPacket)

            for (record in records) {

                val resolvedDeviceId = deviceId ?: continue

                val telemetryEvent =
                    TelemetryEventMapper.map(
                        deviceId = resolvedDeviceId,
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

        try {

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

                                state =
                                    ConnectionState.CONNECTED

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

        } catch (exception: Exception) {

            println(
                "Connection error for device " +
                        "${session?.imei}: ${exception.message}"
            )

        } finally {

            session?.let {

                telemetryRepository.markOffline(it.imei)

                sessionManager.remove(it.imei)

                println(
                    "Device marked offline: ${it.imei}"
                )

                println(
                    "Device removed: ${it.imei}"
                )

                println(
                    "Active devices: ${sessionManager.count()}"
                )
            }

            println(
                "Client disconnected"
            )
        }

        session?.let {

            telemetryRepository.markOffline(it.imei)

            sessionManager.remove(it.imei)

            println(
                "Device marked offline: ${it.imei}"
            )

            println(
                "Device removed: ${it.imei}"
            )

            println(
                "Active devices: ${sessionManager.count()}"
            )
        }

        println(
            "Client disconnected"
        )
    }

    private fun processImei(
        receiveBuffer: PacketBuffer
    ): DeviceSession? {

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

        val packet =
            receiveBuffer.read(packetLength)

        println("IMEI packet received: ${packet.size} bytes")

        val message =
            decoder.decode(packet)

        println("Decoded IMEI message: $message")

        if (message !is ProtocolMessage.Imei) {
            return null
        }

        val device =
            deviceRepository.findByImei(message.value)

        if (device == null || !device.active) {

            println(
                "Unknown or inactive device: ${message.value}"
            )

            socket.getOutputStream().write(
                byteArrayOf(0x00)
            )

            socket.getOutputStream().flush()

            return null
        }

        deviceId = device.id

        val newSession =
            DeviceSession(
                imei = message.value,
                socket = socket
            )

        sessionManager.register(newSession)

        println(
            "Device connected: ${newSession.imei}"
        )

        println(
            "Device ID: ${device.id}"
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

        println(
            "Device accepted"
        )
    }
}