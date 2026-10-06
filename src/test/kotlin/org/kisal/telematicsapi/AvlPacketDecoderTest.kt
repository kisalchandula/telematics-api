package org.kisal.telematicsapi

import org.kisal.telematicsapi.protocol.AvlPacketDecoder
import kotlin.test.assertNotNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AvlPacketDecoderTest {

    private val decoder = AvlPacketDecoder()

    @Test
    fun `decode valid AVL frame`() {

        val packet = byteArrayOf(
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x03,
            0x08,
            0x01,
            0x01,
            0x00, 0x00, 0xCE.toByte(), 0x93.toByte()
        )

        val result = decoder.decode(packet)

        val decoded = assertNotNull(result)

        assertEquals(0x08, decoded.codecId)
        assertEquals(1, decoded.recordCount)
    }

    @Test
    fun `reject invalid preamble`() {

        val packet = byteArrayOf(
            0x01, 0x02, 0x03, 0x04,
            0x00, 0x00, 0x00, 0x04,
            0x08,
            0x01,
            0x01,
            0x00, 0x00, 0x00, 0x00
        )

        val result = decoder.decode(packet)

        assertNull(result)
    }

    @Test
    fun `reject mismatched record counts`() {

        val packet = byteArrayOf(
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x04,
            0x08,
            0x01,
            0x02,
            0x00, 0x00, 0x00, 0x00
        )

        val result = decoder.decode(packet)

        assertNull(result)
    }
}