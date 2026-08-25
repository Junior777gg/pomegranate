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
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.nio.charset.Charset
import kotlin.io.appendText
import kotlin.io.readLines
import kotlin.io.readText
import kotlin.io.writeText

import java.io.File as FileAccess

actual val rootDirectory = System.getProperty("user.dir") ?: ""
actual val separator : String = FileAccess.separator
actual typealias KMPFile = FileAccess
actual fun KMPFile.kmpCopyTo(file: KMPFile) = copyTo(file)

actual fun KMPFile.kmpReadBytes(): ByteArray = FileInputStream(this).use { it.readBytes() }

actual fun KMPFile.kmpReadText(charset: String): String =
    readText(Charset.forName(charset))

actual fun KMPFile.kmpWriteBytes(data: ByteArray): Unit = writeBytes(data)

actual fun KMPFile.kmpAppendBytes(data: ByteArray): Unit = appendBytes(data)

actual fun KMPFile.kmpWriteText(text: String, charset: String) =
    writeText(text, Charset.forName(charset))

actual fun KMPFile.kmpAppendText(text: String, charset: String) =
    appendText(text, Charset.forName(charset))
actual typealias KMPInputStream = InputStream
actual typealias KMPOutputStream = OutputStream
actual typealias KMPByteArrayInputStream = ByteArrayInputStream
actual typealias KMPByteArrayOutputStream = ByteArrayOutputStream
actual fun KMPFile.inputStream(): KMPInputStream = FileInputStream(this)
actual fun KMPFile.outputStream(): KMPOutputStream = FileOutputStream(this)
actual fun ByteArray.inputStream(): KMPInputStream = ByteArrayInputStream(this)

actual class FileSaver {
    actual suspend fun saveFile(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentFile = File(path)
            val userHome = System.getProperty("user.home") ?: return@withContext false
            val downloadsDir = FileAccess(userHome, "Downloads")

            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val fileName = path.substringAfterLast('/').substringAfterLast('\\')
            val targetFile = FileAccess(downloadsDir, fileName)
            currentFile.copyTo(targetFile, overwrite = true)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

actual class ChooseFile actual constructor(){
    companion object {
        lateinit var choose: (onResult: (KMPFile) -> Unit) -> Unit
    }
    actual fun get(onResult: (KMPFile) -> Unit) {
        choose(onResult)
    }
}

actual class ChooseMultipleFiles actual constructor(){
    companion object {
        lateinit var choose: (onResult: (List<KMPFile>) -> Unit) -> Unit
    }
    actual fun get(onResult: (List<KMPFile>) -> Unit) {
        choose(onResult)
    }
}

actual class ChooseImage actual constructor(){
    companion object {
        lateinit var choose: (onResult: (KMPFile) -> Unit) -> Unit
    }
    actual fun get(onResult: (KMPFile) -> Unit) {
        choose(onResult)
    }
}

actual class ChooseMultipleImages actual constructor(){
    companion object {
        lateinit var choose: (onResult: (List<KMPFile>) -> Unit) -> Unit
    }
    actual fun get(onResult: (List<KMPFile>) -> Unit) {
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