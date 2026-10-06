package org.kisal.telematicsapi.config

import org.kisal.telematicsapi.server.GatewayServer
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import kotlin.concurrent.thread

@Component
class GatewayStartup(
    private val gatewayServer: GatewayServer
) : CommandLineRunner {

    override fun run(vararg args: String) {

        thread(
            name = "telematics-tcp-gateway",
            isDaemon = true
        ) {
            gatewayServer.start()
        }
    }
}