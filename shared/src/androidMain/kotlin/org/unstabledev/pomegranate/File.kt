package org.unstabledev.pomegranate

import android.content.Context
import java.io.File

actual class File actual constructor(val path: String) {
    val currentPath = "${context.filesDir}${java.io.File.separator}$path"
    actual fun createFile() {
        val file = File(currentPath)
        file.createNewFile()
    }

    actual fun createDirectory() {
        java.io.File(currentPath).mkdir()
    }

    actual fun readText(): String {
        return java.io.File(currentPath).readText()
    }

    actual fun writeText(text: String) {
        java.io.File(currentPath).writeText(text)
    }

    actual fun readBytes(): ByteArray {
        return java.io.File(currentPath).readBytes()
    }

    actual fun writeBytes(bytes: ByteArray) {
        java.io.File(currentPath).writeBytes(bytes)
    }

    actual fun delete() {
        java.io.File(currentPath).delete()
    }

    actual fun exists(): Boolean {
        return java.io.File(currentPath).exists()
    }

    actual companion object {
        lateinit var context: Context
        actual val sep: String
            get() = java.io.File.separator
    }
}