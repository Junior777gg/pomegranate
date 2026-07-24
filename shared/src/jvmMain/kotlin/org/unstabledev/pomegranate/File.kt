package org.unstabledev.pomegranate

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import coil3.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.net.URI

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

actual suspend fun DroppedFile.readBytes(): ByteArray = withContext(Dispatchers.IO) {
    val uri = URI(uriString)
    FileAccess(uri).readBytes()
}

@Composable
actual fun Modifier.fileDropArea(
    onFilesDropped: (List<DroppedFile>) -> Unit,
    onStarted: () -> Unit,
    onExited: () -> Unit,
): Modifier {
    val latestOnStarted by rememberUpdatedState(onStarted)
    val latestOnExited by rememberUpdatedState(onExited)
    val latestOnFilesDropped by rememberUpdatedState(onFilesDropped)
    val dragTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                latestOnStarted()
            }

            override fun onExited(event: DragAndDropEvent) {
                latestOnExited()
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val files = mutableListOf<DroppedFile>()
                try {
                    val transferable = event.transferableOrNull()
                    if (transferable?.isDataFlavorSupported(DataFlavor.javaFileListFlavor) == true) {
                        @Suppress("UNCHECKED_CAST")
                        val fileList =
                            transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<FileAccess>
                        fileList?.forEach { f ->
                            files += DroppedFile(
                                name = f.name,
                                mimeType = "application/octet-stream",
                                uriString = f.toURI().toString()
                            )
                        }
                    } else if (transferable?.isDataFlavorSupported(DataFlavor.stringFlavor) == true) {
                        val raw = transferable.getTransferData(DataFlavor.stringFlavor) as? String
                        raw?.lineSequence()
                            ?.mapNotNull { it.trim().takeIf { s -> s.isNotBlank() } }
                            ?.map { FileAccess(it) }
                            ?.filter { it.exists() }
                            ?.forEach { f ->
                                files += DroppedFile(
                                    name = f.name,
                                    mimeType = "application/octet-stream",
                                    uriString = f.toURI().toString()
                                )
                            }
                    } else if (transferable?.isDataFlavorSupported(DataFlavor("text/uri-list;class=java.lang.String")) == true) {
                        val data = transferable.getTransferData(DataFlavor("text/uri-list;class=java.lang.String")) as String
                        data.lineSequence()
                            .filter { it.startsWith("file://") }
                            .map { URI.create(it.trim()) }
                            .map(::FileAccess)
                            .forEach {
                                files += DroppedFile(it.name, "application/octet-stream", it.toURI().toString())
                            }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (files.isNotEmpty()) latestOnFilesDropped(files)
                return true
            }
        }
    }
    return dragAndDropTarget(
        shouldStartDragAndDrop = { true },
        target = dragTarget
    )
}

private fun DragAndDropEvent.transferableOrNull(): Transferable? = runCatching {
    val f = this::class.java.getDeclaredField("nativeEvent").apply { isAccessible = true }
    when (val e = f.get(this)) {
        is java.awt.dnd.DropTargetDropEvent -> e.transferable
        is java.awt.dnd.DropTargetDragEvent -> e.transferable
        else -> null
    }
}.getOrNull()

actual fun getBitmapFromBytes(bytes: ByteArray): ImageBitmap = Image.makeFromEncoded(bytes).toComposeImageBitmap()