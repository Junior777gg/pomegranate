package org.unstabledev.pomegranate

import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessagesDao

expect object ConnectionReceiver {
    suspend fun start(chatDao: ChatDao, messagesDao: MessagesDao)
}