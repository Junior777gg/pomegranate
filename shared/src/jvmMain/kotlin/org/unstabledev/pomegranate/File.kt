package org.unstabledev.pomegranate

import java.io.File

actual class File actual constructor(val path: String) {
    val currentPath = "${System.getProperty("user.dir")}${File.separator}$path"
    actual fun createFile() {
        File(currentPath).createNewFile()
    }

    actual fun createDirectory() {
        File(currentPath).mkdir()
    }

    actual fun readText(): String {
        return File(currentPath).readText()
    }

    actual fun writeText(text: String) {
        File(currentPath).writeText(text)
    }

    actual fun readBytes(): ByteArray {
        return File(currentPath).readBytes()
    }

    actual fun writeBytes(bytes: ByteArray) {
        File(currentPath).writeBytes(bytes)
    }

    actual fun delete() {
        File(currentPath).delete()
    }

    actual fun exists(): Boolean {
        return File(currentPath).exists()
    }
    actual companion object {
        actual val sep: String
            get() = File.separator
    }
}