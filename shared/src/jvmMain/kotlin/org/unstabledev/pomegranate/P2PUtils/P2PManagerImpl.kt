package org.unstabledev.pomegranate.P2PUtils

import P2PManager
import kotlinx.coroutines.delay

actual class P2PManagerImpl {
    actual var channel: P2PChannelImpl? = null
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
        val libChannel = manager.createConnection(remoteAddress, remoteLocalAddress, peerPublicKeyJson)
        while (true) {
            try {
                channel = P2PChannelImpl(libChannel)
                return channel!!
            }catch (e : NullPointerException){

            }
            delay(500)
        }
    }

    actual fun breakConnection() {
        manager.breakConnection()
    }
}