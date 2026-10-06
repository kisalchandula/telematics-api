package org.kisal.telematicsapi.server

import org.kisal.telematicsapi.device.DeviceSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.kisal.telematicsapi.domain.TelemetryRepository
import org.kisal.telematicsapi.protocol.AvlPacketDecoder
import org.kisal.telematicsapi.protocol.TeltonikaDecoder
import java.net.ServerSocket

class GatewayServer(
    private val port: Int,
    private val telemetryRepository: TelemetryRepository
) {

    private val sessionManager = DeviceSessionManager()

    fun start() = runBlocking {

        ServerSocket(port).use { serverSocket ->

            println("Listening on port $port")

            while (true) {

                val socket = serverSocket.accept()

                launch(Dispatchers.IO) {

                    val decoder = TeltonikaDecoder()
                    val avlPacketDecoder = AvlPacketDecoder()

                    ClientConnection(
                        socket = socket,
                        decoder = decoder,
                        avlPacketDecoder = avlPacketDecoder,
                        sessionManager = sessionManager,
                        telemetryEventHandler = { event ->
                            telemetryRepository.save(event)
                        }
                    ).handle()
                }
            }
        }
    }
}