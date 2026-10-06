package protocol

sealed interface TeltonikaMessage {

    data class Imei(
        val value: String
    ) : TeltonikaMessage
}