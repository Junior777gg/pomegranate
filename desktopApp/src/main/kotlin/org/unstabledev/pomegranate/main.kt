package org.unstabledev.pomegranate

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.database.getChatDatabase
import org.unstabledev.pomegranate.database.getMessagesDatabase
import java.awt.FileDialog
import java.awt.Frame

fun main(args: Array<String>) {
    val isOpen = mutableStateOf(true)
    val chatBuilder = getChatDatabaseBuilder()
    val chatDatabase = getChatDatabase(chatBuilder)
    val chatDao = chatDatabase.chatDao()
    val messagesBuilder = getMessagesDatabaseBuilder()
    val messagesDatabase = getMessagesDatabase(messagesBuilder)
    val messagesDao = messagesDatabase.messagesDao()
    val runBg = !args.contains("--no-bg-service")
    CoroutineScope(Dispatchers.IO).launch {
        ConnectionReceiver.start(chatDao, messagesDao,)
    }
    ChooseFiles.choose = { onResult ->
        val dialog = FileDialog(null as Frame?, "Выберите файл", FileDialog.LOAD)
        dialog.isMultipleMode = true
        dialog.isVisible = true
        val bytes = dialog.files.toList().map { it.readBytes() to it.extension }
        onResult(bytes)
    }
    application {
        val trayState = rememberTrayState()
        Notifications.currentPush = { title, message ->
            trayState.sendNotification(Notification(title, message))
        }
        if (runBg) {
            Tray(
                state = trayState,
                icon = painterResource("pomegranate.png"),
                menu = {
                    Item("Открыть", onClick = { isOpen.value = true })
                    Item("Выйти", onClick = { exitApplication() })
                }
            )
        }

        if (isOpen.value) {
            Window(
                onCloseRequest = {
                    AppSettings.save()
                    isOpen.value = false
                },
                title = "pomegranate",
                icon = painterResource("pomegranate.png")
            ) {
                App(chatDao, messagesDao)
            }
        }
    }
}

