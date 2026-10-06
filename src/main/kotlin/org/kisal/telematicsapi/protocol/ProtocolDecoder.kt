package protocol

interface ProtocolDecoder {

    fun decode(data: ByteArray): ProtocolMessage?
}