package org.unstabledev.pomegranate

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
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
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import coil3.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import kotlin.io.outputStream
import java.io.File as FileAccess

actual class File actual constructor(val path: String) {
    val currentPath = "${context.filesDir}${FileAccess.separator}$path"
    actual fun createFile() {
        val file = FileAccess(currentPath)
        file.createNewFile()
    }

    actual fun createDirectory() {
        FileAccess(currentPath).mkdir()
    }

    actual fun readText(): String {
        return FileAccess(currentPath).readText()
    }

    actual fun writeText(text: String) {
        FileAccess(currentPath).writeText(text)
    }

    actual fun readBytes(): ByteArray {
        return FileAccess(currentPath).readBytes()
    }

    actual fun writeBytes(bytes: ByteArray) {
        FileAccess(currentPath).writeBytes(bytes)
    }

    actual fun delete() {
        FileAccess(currentPath).delete()
    }

    actual fun exists(): Boolean {
        return FileAccess(currentPath).exists()
    }

    actual companion object {
        lateinit var context: Context
        actual val sep: String
            get() = FileAccess.separator
    }

    actual fun size(): Long {
        val file = FileAccess(currentPath)
        return if (file.exists() && file.isFile) file.length() else 0L
    }
}

actual class FileSaver {
    companion object {
        lateinit var context: Context
    }

    actual suspend fun saveBitmapImage(bitmap: ImageBitmap, fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val androidBitmap = bitmap.asAndroidBitmap()
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/pomegranate"
                    )
                }
            }

            val collectionUri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

            val uri = resolver.insert(collectionUri, contentValues) ?: return@withContext false
            val success = resolver.openOutputStream(uri)?.use { outputStream ->
                androidBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
            } ?: false
            return@withContext success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    actual suspend fun saveBytes(bytes: ByteArray, fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val collectionUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = resolver.insert(collectionUri, contentValues) ?: return@withContext false

                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(bytes)
                } ?: return@withContext false
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = FileAccess(downloadsDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(bytes)
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
        lateinit var choose: (onResult: (List<Pair<ByteArray, String>>) -> Unit) -> Unit
    }

    actual fun getFiles(onResult: (List<Pair<ByteArray, String>>) -> Unit) {
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
