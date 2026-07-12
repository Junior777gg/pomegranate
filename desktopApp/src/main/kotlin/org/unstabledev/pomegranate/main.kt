package org.unstabledev.pomegranate

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.unstabledev.pomegranate.database.getChatDatabase
import org.unstabledev.pomegranate.database.getMessagesDatabase

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "pomegranate",
        icon = painterResource("pomegranate.png")
    ) {
        val chatBuilder = getChatDatabaseBuilder()
        val chatDatabase = getChatDatabase(chatBuilder)
        val chatDao = chatDatabase.chatDao()
        val messagesBuilder = getMessagesDatabaseBuilder()
        val messagesDatabase = getMessagesDatabase(messagesBuilder)
        val messagesDao = messagesDatabase.messagesDao()
        App(chatDao, messagesDao)
    }
}