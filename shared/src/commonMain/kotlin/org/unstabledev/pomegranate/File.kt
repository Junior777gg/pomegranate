package org.unstabledev.pomegranate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap

expect class File(path: String){
    companion object{
        val currentPath : String
        val sep: String
    }
    fun createFile()
    fun createDirectory()
    fun readText(): String
    fun writeText(text: String)
    fun readBytes(): ByteArray
    fun writeBytes(bytes: ByteArray)
    fun delete()
    fun exists(): Boolean
    fun size(): Long
}

expect class FileSaver() {
    suspend fun saveBitmapImage(bitmap: ImageBitmap, fileName: String): Boolean
    suspend fun saveBytes(bytes: ByteArray, fileName: String): Boolean
}

expect class ChooseFiles(){
    fun getFiles(onResult: (List<Pair<ByteArray, String>>) -> Unit)
}

data class DroppedFile(
    val name: String,
    val mimeType: String,
    val uriString: String
)
expect suspend fun DroppedFile.readBytes(): ByteArray

@Composable
expect fun Modifier.fileDropArea(
    onFilesDropped: (List<DroppedFile>) -> Unit,
    onStarted: () -> Unit = {},
    onExited: () -> Unit = {},
): Modifier

expect fun getBitmapFromBytes(bytes: ByteArray): ImageBitmap