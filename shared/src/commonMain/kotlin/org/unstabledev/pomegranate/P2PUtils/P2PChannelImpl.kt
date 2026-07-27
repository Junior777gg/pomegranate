package org.unstabledev.pomegranate.P2PUtils

import kotlinx.coroutines.flow.Flow

expect class P2PChannelImpl(actChannel: Any){
    var remoteIP : String
    var remotePort: Int
    suspend fun send(data: ByteArray)
    suspend fun receive(): ByteArray
}