package org.kisal.telematicsapi.protocol

class Codec8Decoder {

    fun decode(packet: AvlPacket): List<AvlRecord> {

        if (packet.codecId != 0x08) {
            return emptyList()
        }

        val data = packet.data
        val records = mutableListOf<AvlRecord>()

        val dataEnd = data.size - 1

        var offset = 2

        repeat(packet.recordCount) {

            /*
             * GPS data requires 24 bytes.
             */
            if (offset + 24 > dataEnd) {
                return emptyList()
            }

            val timestamp =
                readLong(data, offset)

            offset += 8

            val priority =
                data[offset].toInt() and 0xFF

            offset += 1

            val longitude =
                readInt(data, offset) / 10_000_000.0

            offset += 4

            val latitude =
                readInt(data, offset) / 10_000_000.0

            offset += 4

            val altitude =
                readShort(data, offset)

            offset += 2

            val angle =
                readShort(data, offset)

            offset += 2

            val satellites =
                data[offset].toInt() and 0xFF

            offset += 1

            val speed =
                readShort(data, offset)

            offset += 2

            if (offset + 2 > dataEnd) {
                return emptyList()
            }

            val eventId =
                data[offset].toInt() and 0xFF

            offset += 1

            val totalIoCount =
                data[offset].toInt() and 0xFF

            offset += 1

            val ioElements =
                mutableListOf<IoElement>()

            /*
             * 1-byte IO elements.
             */

            if (offset >= dataEnd) {
                return emptyList()
            }

            val oneByteCount =
                data[offset].toInt() and 0xFF

            offset += 1

            repeat(oneByteCount) {

                if (offset + 2 > dataEnd) {
                    return emptyList()
                }

                val id =
                    data[offset].toInt() and 0xFF

                offset += 1

                val value =
                    data[offset].toLong() and 0xFF

                offset += 1

                ioElements.add(
                    IoElement(
                        id = id,
                        value = value
                    )
                )
            }

            /*
             * 2-byte IO elements.
             */

            if (offset >= dataEnd) {
                return emptyList()
            }

            val twoByteCount =
                data[offset].toInt() and 0xFF

            offset += 1

            repeat(twoByteCount) {

                if (offset + 3 > dataEnd) {
                    return emptyList()
                }

                val id =
                    data[offset].toInt() and 0xFF

                offset += 1

                val value =
                    readUnsignedShort(
                        data,
                        offset
                    )

                offset += 2

                ioElements.add(
                    IoElement(
                        id = id,
                        value = value
                    )
                )
            }

            /*
             * 4-byte IO elements.
             */

            if (offset >= dataEnd) {
                return emptyList()
            }

            val fourByteCount =
                data[offset].toInt() and 0xFF

            offset += 1

            repeat(fourByteCount) {

                if (offset + 5 > dataEnd) {
                    return emptyList()
                }

                val id =
                    data[offset].toInt() and 0xFF

                offset += 1

                val value =
                    readUnsignedInt(
                        data,
                        offset
                    )

                offset += 4

                ioElements.add(
                    IoElement(
                        id = id,
                        value = value
                    )
                )
            }

            /*
             * 8-byte IO elements.
             */

            if (offset >= dataEnd) {
                return emptyList()
            }

            val eightByteCount =
                data[offset].toInt() and 0xFF

            offset += 1

            repeat(eightByteCount) {

                if (offset + 9 > dataEnd) {
                    return emptyList()
                }

                val id =
                    data[offset].toInt() and 0xFF

                offset += 1

                val value =
                    readLong(
                        data,
                        offset
                    )

                offset += 8

                ioElements.add(
                    IoElement(
                        id = id,
                        value = value
                    )
                )
            }

            /*
             * Verify the total IO count.
             */

            if (ioElements.size != totalIoCount) {
                return emptyList()
            }

            records.add(
                AvlRecord(
                    gps = GpsData(
                        timestamp = timestamp,
                        priority = priority,
                        longitude = longitude,
                        latitude = latitude,
                        altitude = altitude,
                        angle = angle,
                        satellites = satellites,
                        speed = speed
                    ),
                    eventId = eventId,
                    ioElements = ioElements
                )
            )
        }

        val secondRecordCount =
            data[dataEnd].toInt() and 0xFF

        if (secondRecordCount != packet.recordCount) {
            return emptyList()
        }

        if (offset != dataEnd) {
            return emptyList()
        }

        return records
    }

    private fun readInt(
        data: ByteArray,
        offset: Int
    ): Int {

        return (
                ((data[offset].toInt() and 0xFF) shl 24) or
                        ((data[offset + 1].toInt() and 0xFF) shl 16) or
                        ((data[offset + 2].toInt() and 0xFF) shl 8) or
                        (data[offset + 3].toInt() and 0xFF)
                )
    }

    private fun readUnsignedInt(
        data: ByteArray,
        offset: Int
    ): Long {

        return (
                ((data[offset].toLong() and 0xFF) shl 24) or
                        ((data[offset + 1].toLong() and 0xFF) shl 16) or
                        ((data[offset + 2].toLong() and 0xFF) shl 8) or
                        (data[offset + 3].toLong() and 0xFF)
                )
    }

    private fun readShort(
        data: ByteArray,
        offset: Int
    ): Int {

        return (
                ((data[offset].toInt() and 0xFF) shl 8) or
                        (data[offset + 1].toInt() and 0xFF)
                ).toShort().toInt()
    }

    private fun readUnsignedShort(
        data: ByteArray,
        offset: Int
    ): Long {

        return (
                ((data[offset].toLong() and 0xFF) shl 8) or
                        (data[offset + 1].toLong() and 0xFF)
                )
    }

    private fun readLong(
        data: ByteArray,
        offset: Int
    ): Long {

        var result = 0L

        for (i in 0 until 8) {

            result =
                (result shl 8) or
                        (data[offset + i].toLong() and 0xFF)
        }

        return result
    }
}