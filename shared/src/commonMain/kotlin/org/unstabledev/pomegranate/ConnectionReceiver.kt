package org.unstabledev.pomegranate

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
    suspend fun start(){
        LoggerImpl().init()
        while (true) {
            val opponent = BaseP2P.receiveConnections()
            val profile = try {
                Gravatar.getProfile(opponent.first.sha256())
            } catch (_: Exception) {
                null
            }
            var chat = Repository.chatDao.tryGetChatByEmailFlow(opponent.first).first()
            if (chat == null) {
                val email = opponent.first
                chat = ChatDC(
                    partnerEmail = listOf(email),
                    profile = mapOf(email to profile?.serialize())
                )
            }


            val observer = Observer(opponent.second, opponent.second.channel!!,chat, Repository.messagesDao)
            Repository.chatDao.upsertChat(chat)
            availableChats.getOrPut(chat, {MutableSharedFlow(1)}).emit(observer)
        }
    }
}