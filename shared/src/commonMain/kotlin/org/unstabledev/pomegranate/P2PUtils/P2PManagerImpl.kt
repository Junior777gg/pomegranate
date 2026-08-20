package org.unstabledev.pomegranate.P2PUtils

expect class P2PManagerImpl(tempDir : String) {
    var channel: P2PChannelImpl?
    fun fork(): P2PManagerImpl
    suspend fun getAddress(): String?
    suspend fun getLocalAddress(): String?
    suspend fun createConnection(remoteAddress: String, remoteLocalAddress: String): P2PChannelImpl
    fun breakConnection()
}