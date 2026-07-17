package org.unstabledev.pomegranate

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.unstabledev.pomegranate.database.getChatDatabase
import org.unstabledev.pomegranate.database.getMessagesDatabase

fun main() {
    val isOpen = mutableStateOf(true)
    val chatBuilder = getChatDatabaseBuilder()
    val chatDatabase = getChatDatabase(chatBuilder)
    val chatDao = chatDatabase.chatDao()
    val messagesBuilder = getMessagesDatabaseBuilder()
    val messagesDatabase = getMessagesDatabase(messagesBuilder)
    val messagesDao = messagesDatabase.messagesDao()
    CoroutineScope(Dispatchers.IO).launch {
        ConnectionReceiver.start(chatDao, messagesDao)
    }
    application {
        Tray(
            icon = painterResource("pomegranate.png"),
            menu = {
                Item("Open", onClick = { isOpen.value = true })
                Item("Exit", onClick = { exitApplication() })
            }
        )

        if (isOpen.value) {
            Window(
                onCloseRequest = { isOpen.value = false },
                title = "pomegranate",
                icon = painterResource("pomegranate.png")
            ) {
                App(chatDao, messagesDao)
            }
        }
    }
}

