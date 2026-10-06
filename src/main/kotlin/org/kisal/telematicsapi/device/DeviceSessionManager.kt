package org.kisal.telematicsapi.device

import java.util.concurrent.ConcurrentHashMap

class DeviceSessionManager {

    private val sessions = ConcurrentHashMap<String, DeviceSession>()

    fun register(session: DeviceSession) {
        sessions[session.imei] = session
    }

    fun get(imei: String): DeviceSession? {
        return sessions[imei]
    }

    fun remove(imei: String) {
        sessions.remove(imei)
    }

    fun count(): Int {
        return sessions.size
    }
}