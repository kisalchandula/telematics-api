package org.kisal.telematicsapi.config

import org.kisal.telematicsapi.domain.DeviceRepository
import org.kisal.telematicsapi.domain.TelemetryRepository
import org.kisal.telematicsapi.server.GatewayServer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayConfig {

    @Bean
    fun gatewayServer(
        telemetryRepository: TelemetryRepository,
        deviceRepository: DeviceRepository
    ): GatewayServer {

        return GatewayServer(
            port = 5000,
            telemetryRepository = telemetryRepository,
            deviceRepository = deviceRepository
        )
    }
}