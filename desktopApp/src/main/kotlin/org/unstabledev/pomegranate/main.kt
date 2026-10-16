package org.unstabledev.pomegranate

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.common.AppSettings
import org.unstabledev.pomegranate.common.BackgroundStorage
import org.unstabledev.pomegranate.database.buildChatDatabase
import org.unstabledev.pomegranate.database.buildMessagesDatabase
import org.unstabledev.pomegranate.database.buildPersonsDatabase
import org.unstabledev.pomegranate.platform.MediaSelector
import org.unstabledev.pomegranate.platform.Notifications
import org.unstabledev.pomegranate.platform.PlatformEvents
import org.unstabledev.pomegranate.platform.shutdownJavaFx
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter
import kotlin.random.Random

fun main(args: Array<String>) {
    AppSettings.load()
    val isOpen = mutableStateOf(true)
    val appState = AppSettings.state.value
    println(appState.currentLink)
    if (appState.currentLink == ""){
        AppSettings.setCurrentLink(Random.nextLong().toString())
        AppSettings.save()
    }
    val chatBuilder = getChatDatabaseBuilder()
    val chatDatabase = buildChatDatabase(chatBuilder)
    val chatDao = chatDatabase.get()
    val messagesBuilder = getMessagesDatabaseBuilder()
    val messagesDatabase = buildMessagesDatabase(messagesBuilder)
    val messagesDao = messagesDatabase.get()
    val personsBuilder = getPersonsDatabaseBuilder()
    val personsDatabase = buildPersonsDatabase(personsBuilder)
    val personsDao = personsDatabase.get()
    val runBg = !args.contains("--no-bg-service")
    CoroutineScope(Dispatchers.IO).launch {
        ConnectionReceiver.start()
    }
    MediaSelector.multipleFilesChoose = { onResult ->
        val dialog = FileDialog(null as Frame?, "Выберите файлы", FileDialog.LOAD)
        dialog.isMultipleMode = true
        dialog.isVisible = true
        val files = dialog.files.toList()
        onResult(files)
    }
    MediaSelector.fileChoose = { onResult ->
        val dialog = FileDialog(null as Frame?, "Выберите файл", FileDialog.LOAD)
        dialog.isMultipleMode = false
        dialog.isVisible = true
        val file = dialog.files.toList().first()
        onResult(file)
    }
    MediaSelector.multipleImagesChoose = { onResult ->
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
    MediaSelector.imageChoose = { onResult ->
        val dialog = FileDialog(null as Frame?, "Выберите изображение", FileDialog.LOAD)
        dialog.isMultipleMode = false
        dialog.filenameFilter = FilenameFilter { dir, name ->
            val lowercaseName = name?.lowercase()?:""
            lowercaseName.endsWith(".jpg") ||
                    lowercaseName.endsWith(".jpeg") ||
                    lowercaseName.endsWith(".png")
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
                    PlatformEvents.Instance?.onAppClose()
                    AppSettings.save()
                    isOpen.value = false
                    if (!runBg) shutdownJavaFx()
                },
                onKeyEvent = {
                    if (it.key==Key.Escape && it.type==KeyEventType.KeyDown) {
                        PlatformEvents.Instance?.onBack()
                    }
                    return@Window false
                },
                title = "pomegranate",
                icon = painterResource("pomegranate.png")
            ) {
                App(chatDao, messagesDao, personsDao)
            }
        }
    }
}

