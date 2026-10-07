package org.kisal.telematicsapi.server

import org.kisal.telematicsapi.device.DeviceSessionManager
import org.kisal.telematicsapi.domain.DeviceRepository
import org.kisal.telematicsapi.domain.TelemetryRepository
import org.kisal.telematicsapi.protocol.AvlPacketDecoder
import org.kisal.telematicsapi.protocol.Codec8Decoder
import org.kisal.telematicsapi.protocol.RawProtocolDecoder
import java.net.ServerSocket
import kotlin.concurrent.thread

class GatewayServer(
    private val port: Int,
    private val telemetryRepository: TelemetryRepository,
    private val deviceRepository: DeviceRepository
) {

    private val sessionManager =
        DeviceSessionManager()

    private val decoder =
        RawProtocolDecoder()

    private val avlPacketDecoder =
        AvlPacketDecoder()

    private val codec8Decoder =
        Codec8Decoder()

    fun start() {

        val serverSocket =
            ServerSocket(port)

        println(
            "Telematics gateway listening on port $port"
        )

        while (true) {

            val socket =
                serverSocket.accept()

            thread(
                name = "device-${socket.port}"
            ) {

                ClientConnection(
                    socket = socket,
                    decoder = decoder,
                    avlPacketDecoder = avlPacketDecoder,
                    sessionManager = sessionManager,
                    deviceRepository = deviceRepository,
                    telemetryRepository = telemetryRepository,
                    codec8Decoder = codec8Decoder,
                    telemetryEventHandler = { event ->

                        telemetryRepository.save(event)

                        println(
                            "Telemetry persisted for device ID " +
                                    "${event.deviceId}"
                        )
                    }
                ).handle()
            }
        }
    }
}
