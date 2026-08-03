package org.unstabledev.pomegranate.P2PUtils

expect class P2PManagerImpl(tempDir : String) {
    var channel: P2PChannelImpl?
    suspend fun getAddress(): String?
    suspend fun getLocalAddress(): String?
    suspend fun getPublicKeyJson(): String
    suspend fun createConnection(remoteAddress: String, remoteLocalAddress: String, peerPublicKeyJson: String): P2PChannelImpl
    fun breakConnection()
}