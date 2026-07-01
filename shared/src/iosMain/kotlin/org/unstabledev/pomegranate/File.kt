package org.unstabledev.pomegranate

actual class File actual constructor(path: String) {
    actual fun createFile() {}
    actual fun createDirectory() {}
    actual fun readText(): String = ""
    actual fun writeText(text: String) {}
    actual fun readBytes(): ByteArray = ByteArray(0)
    actual fun writeBytes(bytes: ByteArray) {}
    actual fun delete() {}
    actual fun exists(): Boolean = false
    actual val sep: String
        get() = ""
}