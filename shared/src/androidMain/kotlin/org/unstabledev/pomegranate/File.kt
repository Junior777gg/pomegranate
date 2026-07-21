package org.unstabledev.pomegranate

import android.content.Context
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

actual class ChooseFiles actual constructor() {
    companion object{
        lateinit var choose : () -> List<Pair<ByteArray, String>>
    }
    actual fun getFiles(): List<Pair<ByteArray, String>> {
        return choose()
    }
}