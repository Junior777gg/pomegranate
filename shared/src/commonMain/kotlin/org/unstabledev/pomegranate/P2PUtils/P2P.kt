package org.unstabledev.pomegranate.P2PUtils

import kotlinx.coroutines.flow.MutableSharedFlow
import org.unstabledev.pomegranate.platform.KMPFile

expect interface Messages

expect class Text(text: String) : Messages {
    val text: String
}

expect class Bytes(bytes: ByteArray) : Messages {
    val bytes: ByteArray
}

expect class Files(file: KMPFile) : Messages {
    val file: KMPFile
}

expect class P2PChannel {
    val tempDir: String
    val receiver: MutableSharedFlow<Array<Messages>>

    suspend fun send(vararg data: Any): Int
    fun closeChannel()
}

expect class ICECandidates {
    val publicKey: String
}

expect class P2PManager(tempDir: String, sendLost : Boolean) {
    val tempDir: String
    var channel: P2PChannel?
    val sendLost : Boolean

    fun getICECandidates(): String
    suspend fun createConnection(iceCandidates: ICECandidates): P2PChannel
    fun fork(sendLost : Boolean): P2PManager?
    fun breakConnection()

    companion object {
        fun getICEFromString(iceCandidates: String): ICECandidates
    }
}