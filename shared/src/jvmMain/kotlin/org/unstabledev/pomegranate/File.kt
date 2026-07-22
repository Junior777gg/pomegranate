package org.unstabledev.pomegranate

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import coil3.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image

import java.io.File as FileAccess

actual class File actual constructor(val path: String) {
    val currentPath = "${System.getProperty("user.dir")}${FileAccess.separator}$path"
    actual fun createFile() {
        FileAccess(currentPath).createNewFile()
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
        actual val sep: String
            get() = FileAccess.separator
    }

    actual fun size(): Long {
        val file = FileAccess(path)
        return if (file.exists() && file.isFile) file.length() else 0L
    }
}

actual class FileSaver {
    actual suspend fun saveBitmapImage(bitmap: ImageBitmap, fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val skiaBitmap = bitmap.asSkiaBitmap()
            val skiaImage = Image.makeFromBitmap(skiaBitmap)
            val encodedData = skiaImage.encodeToData() ?: return@withContext false
            val bytes = encodedData.bytes

            val userHome = System.getProperty("user.home") ?: return@withContext false
            val downloadsDir = FileAccess(userHome, "Downloads")

            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val targetFile = FileAccess(downloadsDir, "$fileName")
            targetFile.writeBytes(bytes)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    actual suspend fun saveBytes(bytes: ByteArray, fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userHome = System.getProperty("user.home") ?: return@withContext false
            val downloadsDir = FileAccess(userHome, "Downloads")

            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val targetFile = FileAccess(downloadsDir, "$fileName")
            targetFile.writeBytes(bytes)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

actual class ChooseFiles actual constructor(){
    companion object {
        lateinit var choose: (onResult: (List<Pair<ByteArray, String>>) -> Unit) -> Unit
    }
    actual fun getFiles(onResult: (List<Pair<ByteArray, String>>) -> Unit) {
        choose(onResult)
    }
}

actual fun getBitmapFromBytes(bytes: ByteArray): ImageBitmap = Image.makeFromEncoded(bytes).toComposeImageBitmap()