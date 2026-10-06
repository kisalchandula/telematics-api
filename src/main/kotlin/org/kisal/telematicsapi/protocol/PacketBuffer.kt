package org.kisal.telematicsapi.protocol

class PacketBuffer {

    private val buffer = ArrayList<Byte>()

    fun append(data: ByteArray) {
        for (byte in data) {
            buffer.add(byte)
        }
    }

    fun size(): Int {
        return buffer.size
    }

    fun peek(index: Int): Byte {
        return buffer[index]
    }

    fun read(length: Int): ByteArray {

        require(length <= buffer.size) {
            "Not enough data in buffer"
        }

        val result = ByteArray(length)

        for (i in 0 until length) {
            result[i] = buffer[i]
        }

        buffer.subList(0, length).clear()

        return result
    }
}