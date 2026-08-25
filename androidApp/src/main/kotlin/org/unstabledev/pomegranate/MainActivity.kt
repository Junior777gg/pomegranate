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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import org.unstabledev.pomegranate.database.getChatDatabase
import org.unstabledev.pomegranate.database.getMessagesDatabase
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        context = this
        Camera.context = this
        Notifications.context = this
        FileSaver.context = this
        Camera.context = this
        AudioPlaybackManager.context = applicationContext
        super.onCreate(savedInstanceState)
        BackgroundStorage.ensureBackgroundDir()
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            {}).apply {
                launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO))
            }
        var pendingFilesResult: ((List<File>) -> Unit)? = null
        val pickFiles = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
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
            pendingFilesResult?.invoke(selectedFiles)
            pendingFilesResult = null
        }
        ChooseMultipleFiles.choose = { onResult ->
            pendingFilesResult = onResult
            pickFiles.launch(arrayOf("*/*"))
        }
        var pendingFileResult: ((File) -> Unit)? = null
        val pickFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            val selectedFile = mutableStateOf<File?>(null)
            if (uri==null) return@registerForActivityResult
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
                selectedFile.value = file
            }
            if (selectedFile.value==null) return@registerForActivityResult
            pendingFileResult?.invoke(selectedFile.value!!)
            pendingFileResult = null
        }
        ChooseFile.choose = { onResult ->
            pendingFileResult = onResult
            pickFile.launch(arrayOf("*/*"))
        }
        var pendingImagesResult: ((List<File>) -> Unit)? = null
        val pickImages = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            val selectedImages = mutableListOf<File>()
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
                    selectedImages.add(file)
                }
            }
            pendingImagesResult?.invoke(selectedImages)
            pendingImagesResult = null
        }
        ChooseMultipleImages.choose = { onResult ->
            pendingImagesResult = onResult
            pickImages.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        var pendingImageResult: ((File) -> Unit)? = null
        val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            val selectedFile = mutableStateOf<File?>(null)
            if (uri==null) return@registerForActivityResult
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
                selectedFile.value = file
            }
            if (selectedFile.value==null) return@registerForActivityResult
            pendingImageResult?.invoke(selectedFile.value!!)
            pendingImageResult = null
        }
        ChooseImage.choose = { onResult ->
            pendingImageResult = onResult
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        val chatBuilder = getChatDatabaseBuilder(applicationContext)
        val chatDatabase = getChatDatabase(chatBuilder)
        val chatDao = chatDatabase.chatDao()
        val messagesBuilder = getMessagesDatabaseBuilder(applicationContext)
        val messagesDatabase = getMessagesDatabase(messagesBuilder)
        val messagesDao = messagesDatabase.messagesDao()
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