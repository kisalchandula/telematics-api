package protocol

data class GpsData(
    val timestamp: Long,
    val priority: Int,
    val longitude: Double,
    val latitude: Double,
    val altitude: Int,
    val angle: Int,
    val satellites: Int,
    val speed: Int
)