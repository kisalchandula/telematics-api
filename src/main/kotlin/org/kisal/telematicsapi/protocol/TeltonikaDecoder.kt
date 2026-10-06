package org.kisal.telematicsapi.protocol

class TeltonikaDecoder : ProtocolDecoder {

    override fun decode(data: ByteArray): ProtocolMessage? {

        if (data.size < 2) {
            println("Not enough data for IMEI length")
            return null
        }

        val imeiLength =
            ((data[0].toInt() and 0xFF) shl 8) or
                    (data[1].toInt() and 0xFF)


        if (data.size < 2 + imeiLength) {
            println("Not enough data for IMEI")
            return null
        }

        val imei = data
            .copyOfRange(2, 2 + imeiLength)
            .toString(Charsets.US_ASCII)

        return ProtocolMessage.Imei(imei)
    }
}