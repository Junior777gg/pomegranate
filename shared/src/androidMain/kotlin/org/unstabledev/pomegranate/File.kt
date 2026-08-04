package org.unstabledev.pomegranate

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.unstabledev.pomegranate.readBytes
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import kotlin.io.outputStream
import java.io.File as FileAccess


var context: Context? = null
actual val rootDirectory by lazy {
    while (context == null){}
    context!!.cacheDir?.absolutePath ?: ""}
actual val separator : String = FileAccess.separator
actual typealias KMPFile = FileAccess
actual fun KMPFile.readBytes(): ByteArray = FileInputStream(this).use { it.readBytes() }

actual fun KMPFile.readText(charset: String): String =
    readText(Charset.forName(charset))

actual fun KMPFile.readLines(charset: String): List<String> =
    readLines(Charset.forName(charset))

actual fun KMPFile.writeBytes(data: ByteArray): Unit = writeBytes(data)

actual fun KMPFile.appendBytes(data: ByteArray): Unit = appendBytes(data)

actual fun KMPFile.writeText(text: String, charset: String) =
    writeText(text, Charset.forName(charset))

actual fun KMPFile.appendText(text: String, charset: String) =
    appendText(text, Charset.forName(charset))

actual typealias KMPInputStream = InputStream
actual typealias KMPOutputStream = OutputStream
actual typealias KMPByteArrayInputStream = ByteArrayInputStream
actual typealias KMPByteArrayOutputStream = ByteArrayOutputStream
actual fun KMPFile.inputStream(): KMPInputStream = FileInputStream(this)
actual fun KMPFile.outputStream(): KMPOutputStream = FileOutputStream(this)
actual fun ByteArray.inputStream(): KMPInputStream = ByteArrayInputStream(this)

actual class FileSaver {
    companion object {
        lateinit var context: Context
    }
    actual suspend fun saveFile(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val currentFile = File(path)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, currentFile.name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val collectionUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = resolver.insert(collectionUri, contentValues) ?: return@withContext false

                resolver.openOutputStream(uri)?.use { outputStream ->
                    currentFile.inputStream().copyTo(outputStream)
                } ?: return@withContext false
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = FileAccess(downloadsDir, currentFile.name)
                FileOutputStream(file).use { outputStream ->
                    currentFile.inputStream().copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

actual class ChooseFiles actual constructor() {
    companion object {
        lateinit var choose: (onResult: (List<KMPFile>) -> Unit) -> Unit
    }

    actual fun getFiles(onResult: (List<KMPFile>) -> Unit) {
        return choose(onResult)
    }
}

actual suspend fun DroppedFile.readBytes(): ByteArray = withContext(Dispatchers.IO) {
    val context = FileSaver.context
    val uri = Uri.parse(uriString)

    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        inputStream.readBytes()
    } ?: throw IllegalStateException("Could not resolve content URI: $uriString")
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
actual fun Modifier.fileDropArea(
    onFilesDropped: (List<DroppedFile>) -> Unit,
    onStarted: () -> Unit,
    onExited: () -> Unit,
): Modifier {
    val context  = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope    = rememberCoroutineScope()
    val dropped by rememberUpdatedState(onFilesDropped)
    val entered by rememberUpdatedState(onStarted)
    val left    by rememberUpdatedState(onExited)

    val target = remember {
        object : DragAndDropTarget {
            override fun onEntered(event: DragAndDropEvent) = entered()
            override fun onExited(event: DragAndDropEvent)  = left()
            override fun onEnded(event: DragAndDropEvent)   = left()

            override fun onDrop(event: DragAndDropEvent): Boolean {
                left()
                val act = activity ?: run { println("drop: no activity"); return false }
                val dragEvent = event.toAndroidDragEvent()
                val perms = ActivityCompat.requestDragAndDropPermissions(act, dragEvent)
                    ?: run { println("drop: permission denied"); return false }

                val clip = dragEvent.clipData ?: run { perms.release(); return false }
                val uris = (0 until clip.itemCount).mapNotNull { clip.getItemAt(it).uri }
                if (uris.isEmpty()) { perms.release(); return false }

                scope.launch {
                    val files = withContext(Dispatchers.IO) {
                        val cr = act.contentResolver
                        uris.mapIndexedNotNull { i, uri ->
                            runCatching {
                                val name = "file_$i"
                                val out = FileAccess(act.cacheDir, "drop_${System.nanoTime()}_$name")
                                cr.openInputStream(uri)!!.use { input ->
                                    out.outputStream().use { input.copyTo(it) }
                                }
                                DroppedFile(
                                    name = name,
                                    mimeType = cr.getType(uri) ?: "application/octet-stream",
                                    uriString = Uri.fromFile(out).toString()
                                )
                            }.onFailure { it.printStackTrace() }.getOrNull()
                        }
                    }
                    perms.release()
                    if (files.isNotEmpty()) dropped(files)
                }
                return true
            }
        }
    }

    return dragAndDropTarget(shouldStartDragAndDrop = { true }, target = target)
}

actual fun getBitmapFromBytes(bytes: ByteArray): ImageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
