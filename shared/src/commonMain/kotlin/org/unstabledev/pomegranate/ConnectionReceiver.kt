package org.unstabledev.pomegranate

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.unstabledev.pomegranate.P2PUtils.LoggerImpl
import org.unstabledev.pomegranate.P2PUtils.Observer
import org.unstabledev.pomegranate.Repository.availableChats
import org.unstabledev.pomegranate.api.Gravatar
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessagesDao
import org.unstabledev.pomegranate.database.serialize
import org.unstabledev.pomegranate.database.sha256

object ConnectionReceiver {
    suspend fun start(chatDao: ChatDao, messagesDao: MessagesDao){
        LoggerImpl().init()
        while (true) {
            val opponent = BaseP2P.receiveConnections()
            val profile = try {
                if(!opponent.first.contains("@")||!opponent.first.contains(".")) null
                Gravatar.getProfile(opponent.first.sha256())
            } catch (_: Exception) {
                null
            }
            val chat = ChatDC(opponent.first, profile?.serialize())
            val observer = Observer(opponent.second, opponent.second.channel!!,chat, messagesDao)
            chatDao.upsertChat(chat)
            availableChats.getOrPut(chat, {MutableSharedFlow(1)}).emit(observer)
        }
    }
}