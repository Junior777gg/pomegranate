package org.unstabledev.pomegranate

import org.unstabledev.pomegranate.P2PUtils.Observer
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessagesDao
import org.unstabledev.pomegranate.database.serialize
import org.unstabledev.pomegranate.database.sha256

actual object ConnectionReceiver {
    actual suspend fun start(chatDao: ChatDao, messagesDao: MessagesDao) {
        while (true) {
            val opponent = BaseP2P.receiveConnections()
            val profile = try {
                Gravatar.getProfile(opponent.first.sha256())
            } catch (_: Exception) {
                null
            }
            val chat = ChatDC(opponent.first, profile?.serialize())
            val observer = Observer(opponent.second, chat, messagesDao)
            chatDao.upsertChat(chat)
            Repository.availableChats[chat] = observer

        }
    }
}