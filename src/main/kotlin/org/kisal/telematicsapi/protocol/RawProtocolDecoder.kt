package org.kisal.telematicsapi.protocol

class RawProtocolDecoder : ProtocolDecoder {

    override fun decode(data: ByteArray): ProtocolMessage? {

        if (data.size < 2) {
            return null
        }

        val imeiLength =
            ((data[0].toInt() and 0xFF) shl 8) or
                    (data[1].toInt() and 0xFF)

        if (data.size != imeiLength + 2) {
            return null
        }

        val imei =
            String(
                data,
                2,
                imeiLength,
                Charsets.US_ASCII
            )

        return ProtocolMessage.Imei(imei)
    }
}