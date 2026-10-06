package org.kisal.telematicsapi.protocol

interface ProtocolDecoder {

    fun decode(data: ByteArray): ProtocolMessage?
}