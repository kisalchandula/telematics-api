package protocol

object Crc16 {

    fun calculate(data: ByteArray): Int {

        var crc = 0x0000

        for (byte in data) {

            crc = crc xor (byte.toInt() and 0xFF)

            repeat(8) {

                if ((crc and 0x0001) != 0) {
                    crc = (crc shr 1) xor 0x8408
                } else {
                    crc = crc shr 1
                }
            }
        }

        return crc and 0xFFFF
    }
}