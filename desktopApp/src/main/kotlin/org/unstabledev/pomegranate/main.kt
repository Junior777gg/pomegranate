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
import java.io.File
import java.io.FilenameFilter

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
        ConnectionReceiver.start()
    }
    ChooseMultipleFiles.choose = { onResult ->
        val dialog = FileDialog(null as Frame?, "Выберите файлы", FileDialog.LOAD)
        dialog.isMultipleMode = true
        dialog.isVisible = true
        val files = dialog.files.toList()
        onResult(files)
    }
    ChooseFile.choose = { onResult ->
        val dialog = FileDialog(null as Frame?, "Выберите файл", FileDialog.LOAD)
        dialog.isMultipleMode = false
        dialog.isVisible = true
        val file = dialog.files.toList().first()
        onResult(file)
    }
    ChooseMultipleImages.choose = { onResult ->
        val dialog = FileDialog(null as Frame?, "Выберите изображения", FileDialog.LOAD)
        dialog.isMultipleMode = true
        dialog.filenameFilter = object : FilenameFilter {
            override fun accept(dir: File?, name: String?): Boolean {
                val lowercaseName = name?.lowercase()?:""
                return lowercaseName.endsWith(".jpg") ||
                        lowercaseName.endsWith(".jpeg") ||
                        lowercaseName.endsWith(".png")
            }
        }
        dialog.isVisible = true
        val files = dialog.files.toList()
        onResult(files)
    }
    ChooseImage.choose = { onResult ->
        val dialog = FileDialog(null as Frame?, "Выберите изображение", FileDialog.LOAD)
        dialog.isMultipleMode = false
        dialog.filenameFilter = object : FilenameFilter {
            override fun accept(dir: File?, name: String?): Boolean {
                val lowercaseName = name?.lowercase()?:""
                return lowercaseName.endsWith(".jpg") ||
                        lowercaseName.endsWith(".jpeg") ||
                        lowercaseName.endsWith(".png")
            }
        }
        dialog.isVisible = true
        val file = dialog.files.toList().first()
        onResult(file)
    }
    BackgroundStorage.ensureBackgroundDir()
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
                    if (!runBg) shutdownJavaFx()
                },
                title = "pomegranate",
                icon = painterResource("pomegranate.png")
            ) {
                App(chatDao, messagesDao)
            }
        }
    }
}

