package org.unstabledev.pomegranate

import android.Manifest
import android.content.Intent
import android.hardware.camera2.CameraDevice
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import org.unstabledev.pomegranate.database.getChatDatabase
import org.unstabledev.pomegranate.database.getMessagesDatabase
import java.io.File
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        context = this
        Camera.context = this
        Notifications.context = this
        FileSaver.context = this
        super.onCreate(savedInstanceState)
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            {}).launch(Manifest.permission.POST_NOTIFICATIONS)
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            {}).launch(Manifest.permission.CAMERA)
        var pendingFileResult: ((List<File>) -> Unit)? = null
        val pick = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            val selectedFiles = mutableListOf<File>()
            uris.forEach { uri ->
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }

                val name = getNameFromUri(uri)

                contentResolver.openInputStream(uri)?.use { input ->
                    val file = File("${Repository.pomegranatePath}temp", name).apply {
                        createNewFile()
                        outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    selectedFiles.add(file)
                }
            }
            pendingFileResult?.invoke(selectedFiles)
            pendingFileResult = null
        }
        ChooseFiles.choose = { onResult ->
            pendingFileResult = onResult
            pick.launch(arrayOf("*/*"))
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

    private fun getNameFromUri(uri: Uri): String {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                val name = cursor.getString(nameIndex)
                if (!name.isNullOrEmpty() && name.contains(".")) {
                    return name.lowercase()
                }
            }
        }
        val mimeType = contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: ""
    }
}