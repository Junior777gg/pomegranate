package org.unstabledev.pomegranate

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

actual class ChooseFiles actual constructor(){
    companion object{
        lateinit var choose : () -> List<Pair<ByteArray, String>>
    }
    actual fun getFiles(): List<Pair<ByteArray, String>> {
        return choose()
    }
}