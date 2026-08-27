package org.unstabledev.pomegranate.P2PUtils

import org.unstabledev.pomegranate.KMPFile
import org.unstabledev.pomegranate.KMPInputStream

sealed class Data {
    class Bytes(val bytes: ByteArray, val code: Byte) : Data()
    class Files(val file: KMPFile, val code: Byte) : Data()
}

expect class P2PChannelImpl(actChannel: Any) {
    suspend fun send(file: KMPFile, code: Byte)
    suspend fun send(stream: KMPInputStream, code: Byte)
    suspend fun send(bytes: ByteArray, code: Byte = 0)
    suspend fun receive(): Data
}