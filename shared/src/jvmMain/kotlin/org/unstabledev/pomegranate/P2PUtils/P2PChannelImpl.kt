package org.unstabledev.pomegranate.P2PUtils

import P2PChannel
import kotlinx.coroutines.flow.Flow

actual class P2PChannelImpl actual constructor(actChannel: Any) {
    val channel = actChannel as P2PChannel
    actual var remoteIP: String
        get() = channel.remoteIp
        set(value) {
            channel.remoteIp = value
        }
    actual var remotePort: Int
        get() = channel.remotePort
        set(value) {
            channel.remotePort = value
        }

    actual suspend fun receive():Pair<Boolean, ByteArray> {
        return channel.receive()
    }

    actual suspend fun send(isPath: Boolean, data: ByteArray) {
        channel.send(isPath, data)
    }

}