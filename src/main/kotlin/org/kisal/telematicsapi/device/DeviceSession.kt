package device

import java.net.Socket
import java.time.Instant

class DeviceSession(
    val imei: String,
    val socket: Socket
) {

    val connectedAt: Instant = Instant.now()

    var lastSeen: Instant = connectedAt
        private set

    fun updateLastSeen() {
        lastSeen = Instant.now()
    }

    fun isConnected(): Boolean {
        return !socket.isClosed
    }
}