package org.unstabledev.pomegranate

import kotlinx.coroutines.flow.MutableStateFlow
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessagesDao

actual object ConnectionReceiver {
    actual suspend fun start(chatDao: ChatDao, messagesDao: MessagesDao) {
    }
}