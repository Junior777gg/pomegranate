package org.unstabledev.pomegranate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap

expect val rootDirectory: String
expect val separator : String
expect open class KMPFile {
    constructor(path: String)
    constructor(parent: String, child: String)
    constructor(parent: KMPFile, child: String)

    // Информация о файле
    fun getName(): String
    fun getParent(): String?
    fun getParentFile(): KMPFile?
    fun getPath(): String
    fun getAbsolutePath(): String
    fun getAbsoluteFile(): KMPFile
    fun getCanonicalPath(): String
    fun getCanonicalFile(): KMPFile

    // Проверки
    fun exists(): Boolean
    fun isDirectory(): Boolean
    fun isFile(): Boolean
    fun isHidden(): Boolean
    fun isAbsolute(): Boolean

    // Права
    fun canRead(): Boolean
    fun canWrite(): Boolean
    fun canExecute(): Boolean
    fun setReadOnly(): Boolean
    fun setWritable(writable: Boolean): Boolean
    fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean
    fun setReadable(readable: Boolean): Boolean
    fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean
    fun setExecutable(executable: Boolean): Boolean
    fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean

    // Создание / удаление
    fun createNewFile(): Boolean
    fun delete(): Boolean
    fun deleteOnExit()

    // Директории
    fun mkdir(): Boolean
    fun mkdirs(): Boolean

    // Метаданные
    fun length(): Long
    fun lastModified(): Long
    fun setLastModified(time: Long): Boolean

    // Листинг
    fun list(): Array<String>?
    fun listFiles(): Array<KMPFile>?

    // Переименование
    fun renameTo(dest: KMPFile): Boolean

    // Свободное место
    fun getTotalSpace(): Long
    fun getFreeSpace(): Long
    fun getUsableSpace(): Long

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
}

expect fun KMPFile.readBytes(): ByteArray
expect fun KMPFile.readText(charset: String = "UTF-8"): String
expect fun KMPFile.readLines(charset: String = "UTF-8"): List<String>
expect fun KMPFile.writeBytes(data: ByteArray)
expect fun KMPFile.appendBytes(data: ByteArray)
expect fun KMPFile.writeText(text: String, charset: String = "UTF-8")
expect fun KMPFile.appendText(text: String, charset: String = "UTF-8")
expect fun KMPFile.inputStream(): KMPInputStream
expect fun KMPFile.outputStream(): KMPOutputStream
expect fun ByteArray.inputStream(): KMPInputStream

expect abstract class KMPInputStream protected constructor() {
    abstract fun read(): Int
    open fun read(b: ByteArray): Int
    open fun read(b: ByteArray, off: Int, len: Int): Int
    open fun skip(n: Long): Long
    open fun available(): Int
    open fun close()
    open fun mark(readlimit: Int)
    open fun reset()
    open fun markSupported(): Boolean
}
expect open class KMPByteArrayInputStream(buf: ByteArray) : KMPInputStream {
    open override fun read(): Int
    open override fun read(b: ByteArray, off: Int, len: Int): Int
    open override fun skip(n: Long): Long
    open override fun available(): Int
    open override fun markSupported(): Boolean
    open override fun mark(readlimit: Int)
    open override fun reset()
}
expect abstract class KMPOutputStream protected constructor() {
    abstract fun write(b: Int)
    open fun write(b: ByteArray)
    open fun write(b: ByteArray, off: Int, len: Int)
    open fun flush()
    open fun close()
}

expect open class KMPByteArrayOutputStream() : KMPOutputStream {
    constructor(size: Int)
    open override fun write(b: Int)
    open override fun write(b: ByteArray, off: Int, len: Int)
    open fun toByteArray(): ByteArray
    open fun size(): Int
    open override fun close()
    open override fun flush()
    open fun reset()
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