package org.unstabledev.pomegranate.P2PUtils

import P2PManager
import kotlinx.coroutines.delay

actual class P2PManagerImpl actual constructor(val tempDir: String) {
    actual var channel: P2PChannelImpl? = null
    var manager = P2PManager(tempDir)

    actual suspend fun createConnection(
        iceCandidates: String,
    ): P2PChannelImpl {
        val libChannel = manager.createConnection(P2PManager.getICEFromString(iceCandidates))
        while (true) {
            try {
                channel = P2PChannelImpl(libChannel)
                return channel!!
            } catch (_: NullPointerException) {

            }
            delay(500)
        }
    }

    actual fun breakConnection() {
        manager.breakConnection()
    }

    actual suspend fun fork(): P2PManagerImpl {
        val newLibManager = manager.fork()
        val newManager = P2PManagerImpl(tempDir)
        newManager.manager = newLibManager!!
        newManager.channel = P2PChannelImpl(newLibManager.channel)
        return newManager

    }

    actual fun getICECandidates(): String {
        return manager.getICECandidates()
    }

}