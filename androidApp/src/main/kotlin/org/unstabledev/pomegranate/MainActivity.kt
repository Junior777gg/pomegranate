package org.unstabledev.pomegranate

import android.Manifest
import android.content.Intent
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        Notifications.context = this
        FileSaver.context = this
        File.context = this
        super.onCreate(savedInstanceState)

        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            {}).launch(Manifest.permission.POST_NOTIFICATIONS)

        var pendingFileResult: ((List<Pair<ByteArray, String>>) -> Unit)? = null
        val pick = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            val selectedBytes = mutableListOf<Pair<ByteArray, String>>()
            uris?.forEach { uri ->
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }

                val extension = getExtensionFromUri(uri)

                contentResolver.openInputStream(uri)?.use { input ->
                    selectedBytes.add(input.readBytes() to extension)
                }
            }
            pendingFileResult?.invoke(selectedBytes)
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

    private fun getExtensionFromUri(uri: Uri): String {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                val name = cursor.getString(nameIndex)
                if (!name.isNullOrEmpty() && name.contains(".")) {
                    return name.substringAfterLast('.').lowercase()
                }
            }
        }
        val mimeType = contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: ""
    }
}