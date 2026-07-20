package org.unstabledev.pomegranate.P2PUtils

import P2PChannel

actual class P2PChannelImpl actual constructor(
    actChannel: Any
){

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

    actual suspend fun send(data: ByteArray) {
        channel.send(data)
    }

    actual suspend fun receive(): ByteArray {
        return channel.receive()
    }

}