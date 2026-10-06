package org.kisal.telematicsapi.protocol

class AvlPacketDecoder {

    fun decode(data: ByteArray): AvlPacket? {

        /*
         * Minimum structure:
         *
         * 4 bytes  -> preamble
         * 4 bytes  -> data length
         * 1 byte   -> codec ID
         * 1 byte   -> number of records
         * 1 byte   -> number of records 2
         * 4 bytes  -> CRC
         */

        if (data.size < 15) {
            return null
        }

        // Check preamble
        for (i in 0 until 4) {
            if (data[i].toInt() != 0) {
                return null
            }
        }

        val dataLength =
            ((data[4].toInt() and 0xFF) shl 24) or
                    ((data[5].toInt() and 0xFF) shl 16) or
                    ((data[6].toInt() and 0xFF) shl 8) or
                    (data[7].toInt() and 0xFF)

        val expectedPacketLength =
            8 + dataLength + 4

        if (data.size < expectedPacketLength) {
            return null
        }

        val codecId =
            data[8].toInt() and 0xFF

        val recordCount =
            data[9].toInt() and 0xFF

        val secondRecordCountIndex =
            8 + dataLength - 1

        val secondRecordCount =
            data[secondRecordCountIndex].toInt() and 0xFF

        if (recordCount != secondRecordCount) {
            return null
        }

        val crcIndex =
            8 + dataLength

        val crc =
            ((data[crcIndex].toLong() and 0xFF) shl 24) or
                    ((data[crcIndex + 1].toLong() and 0xFF) shl 16) or
                    ((data[crcIndex + 2].toLong() and 0xFF) shl 8) or
                    (data[crcIndex + 3].toLong() and 0xFF)

        val payloadStart = 8

        val payloadEnd = 8 + dataLength

        val payload =
            data.copyOfRange(
                payloadStart,
                payloadEnd
            )

        val calculatedCrc =
            Crc16.calculate(payload)

        if (calculatedCrc.toLong() != crc) {
            return null
        }

        return AvlPacket(
            codecId = codecId,
            recordCount = recordCount,
            data = payload,
            crc = crc
        )
    }
}