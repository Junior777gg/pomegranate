package org.unstabledev.pomegranate.P2PUtils

import P2PChannel
import kotlinx.coroutines.flow.Flow
import org.unstabledev.pomegranate.KMPFile
import org.unstabledev.pomegranate.KMPInputStream

actual class P2PChannelImpl actual constructor(
    actChannel: Any
){

    val channel = actChannel as P2PChannel
    actual suspend fun send(file: KMPFile, code: Byte) {
        channel.send(file, code)
    }

    actual suspend fun send(bytes: ByteArray, code: Byte) {
        channel.send(bytes, code)
    }

    actual suspend fun receive(): Data {
        val data = channel.receive()
        return when (data) {
            is Messages.ByteMessage -> Data.Bytes(data.bytes, data.code)
            is Messages.FileMessage -> Data.Files(data.file, data.code)
        }
    }

    actual suspend fun send(stream: KMPInputStream, code: Byte) {
    }
}