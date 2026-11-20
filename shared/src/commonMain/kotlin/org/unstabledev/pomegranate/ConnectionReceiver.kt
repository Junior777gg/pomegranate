package org.unstabledev.pomegranate

import kotlinx.coroutines.flow.MutableSharedFlow
import org.unstabledev.pomegranate.P2PUtils.LoggerImpl
import org.unstabledev.pomegranate.P2PUtils.Observer
import org.unstabledev.pomegranate.Repository.availablePersons

object ConnectionReceiver {
    private var canRun=false
    private var isRunning=false

    fun stop() { canRun=false }
    fun isRunning() = isRunning
    fun toggle(v: Boolean) { canRun=v }

    suspend fun start() {
        canRun=true
        isRunning=true
        LoggerImpl().init()
        while (canRun) {
            val opponent = BaseP2P.receiveConnections()
            val email = opponent.first
            val observer = Observer(opponent.second, opponent.second.channel!!,email)
            availablePersons.getOrPut(email){MutableSharedFlow(1)}.emit(observer)
        }
        isRunning=false
    }
}