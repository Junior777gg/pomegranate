package org.unstabledev.pomegranate

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import org.unstabledev.pomegranate.database.getChatDatabase
import org.unstabledev.pomegranate.database.getMessagesDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        Notifications.context = this
        File.context = this
        super.onCreate(savedInstanceState)
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            {}).launch(Manifest.permission.POST_NOTIFICATIONS)
        val bytes = mutableListOf<Pair<ByteArray, String>>()
        val pick = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
                uris ->
            uris.forEach {uri ->
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val file = uri.toFile()
                bytes.add(file.readBytes() to file.extension)
            }
        }
        ChooseFiles.choose = {
            pick.launch(arrayOf("*/*"))
            bytes
        }
        val chatBuilder = getChatDatabaseBuilder(applicationContext)
        val chatDatabase = getChatDatabase(chatBuilder)
        val chatDao = chatDatabase.chatDao()
        val messagesBuilder = getMessagesDatabaseBuilder(applicationContext)
        val messagesDatabase = getMessagesDatabase(messagesBuilder)
        val messagesDao = messagesDatabase.messagesDao()
        ReceiverService.chatDao = chatDao
        ReceiverService.messagesDao = messagesDao
        baseContext.startForegroundService(Intent(applicationContext, ReceiverService::class.java))
        setContent {
            App(chatDao, messagesDao)
        }
    }
}