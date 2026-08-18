package org.unstabledev.pomegranate.P2PUtils

import P2PManager
import kotlinx.coroutines.delay

actual class P2PManagerImpl actual constructor(tempDir: String) {
    actual var channel: P2PChannelImpl? = null
    var manager = P2PManager(tempDir)
    actual suspend fun getAddress(): String? {
        return manager.getAddress()
    }

    actual suspend fun getLocalAddress(): String? {
        return manager.getLocalAddress()
    }

    actual suspend fun createConnection(
        remoteAddress: String,
        remoteLocalAddress: String,
    ): P2PChannelImpl {
        val libChannel = manager.createConnection(remoteAddress, remoteLocalAddress)
        while (true) {
            try {
                channel = P2PChannelImpl(libChannel)
                return channel!!
            }catch (_ : NullPointerException){

            }
            delay(500)
        }
    }

    actual fun breakConnection() {
        manager.breakConnection()
    }

    actual suspend fun fork(): P2PManagerImpl? {
        val oldManager = manager   // сохраняем старый, рабочий менеджер
        val newP2P = oldManager.fork() ?: return null
        val impl = P2PManagerImpl(manager.tempDir)
        impl.manager = newP2P
        return impl
    }
}