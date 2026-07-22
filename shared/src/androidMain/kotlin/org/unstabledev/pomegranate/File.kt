package org.unstabledev.pomegranate

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil3.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
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

actual fun getBitmapFromBytes(bytes: ByteArray): ImageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
