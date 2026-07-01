package org.unstabledev.pomegranate.P2PUtils

import P2PChannel
import P2PManager

actual class P2PManagerImpl {
    private lateinit var channel: P2PChannel
    val manager = P2PManager()
    actual suspend fun getAddress(): String? {
        return manager.getAddress()
    }

    actual suspend fun getLocalAddress(): String? {
        return manager.getLocalAddress()
    }

    actual suspend fun getPublicKeyJson(): String {
        return manager.getPublicKeyJson()
    }

    actual suspend fun createConnection(
        remoteAddress: String,
        remoteLocalAddress: String,
        peerPublicKeyJson: String
    ): P2PChannelImpl {
        channel = manager.createConnection(remoteAddress, remoteLocalAddress, peerPublicKeyJson)
        return P2PChannelImpl(channel)
    }

    actual fun breakConnection() {
        manager.breakConnection()
    }
}