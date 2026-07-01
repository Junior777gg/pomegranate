package org.unstabledev.pomegranate.P2PUtils

actual class P2PChannelImpl {
    actual var remoteIP: String
        get() = ""
        set(value) {}
    actual var remotePort: Int
        get() = 0
        set(value) {}

    actual suspend fun send(data: ByteArray) {
    }

    actual suspend fun receive(): ByteArray {
        return ByteArray(0)
    }

}