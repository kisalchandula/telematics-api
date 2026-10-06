package protocol

import org.kisal.telematicsapi.protocol.AvlRecord

data class AvlPacket(
    val codecId: Int,
    val recordCount: Int,
    val data: ByteArray,
    val crc: Long,
    val records: List<AvlRecord> = emptyList()
)