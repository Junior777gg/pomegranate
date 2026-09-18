package org.unstabledev.pomegranate.P2PUtils

expect class P2PManagerImpl(tempDir : String) {
    var channel: P2PChannelImpl?
    suspend fun fork(): P2PManagerImpl
    fun getICECandidates(): String
    suspend fun createConnection(iceCandidates: String): P2PChannelImpl
    fun breakConnection()
}