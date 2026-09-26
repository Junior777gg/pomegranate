package org.unstabledev.pomegranate

import kotlinx.coroutines.flow.MutableSharedFlow
import org.unstabledev.pomegranate.P2PUtils.LoggerImpl
import org.unstabledev.pomegranate.P2PUtils.Observer
import org.unstabledev.pomegranate.Repository.availablePersons

object ConnectionReceiver {
    suspend fun start(){
        LoggerImpl().init()
        while (true) {
            val opponent = BaseP2P.receiveConnections()
            val email = opponent.first
            val observer = Observer(opponent.second, opponent.second.channel!!,email, Repository.messagesDao)
            availablePersons.getOrPut(email){MutableSharedFlow(1)}.emit(observer)
        }
    }
}