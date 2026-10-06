package org.kisal.telematicsapi.protocol

sealed interface ProtocolMessage {

    data class Imei(
        val value: String
    ) : ProtocolMessage
}