package org.unstabledev.pomegranate.P2PUtils

actual class P2PManagerImpl {
    actual suspend fun getAddress(): String? = ""
    actual suspend fun getLocalAddress(): String? = ""
    actual suspend fun getPublicKeyJson(): String = ""
    actual suspend fun createConnection(
        remoteAddress: String,
        remoteLocalAddress: String,
        peerPublicKeyJson: String
    ): P2PChannelImpl = P2PChannelImpl()
    actual fun breakConnection() {}
}