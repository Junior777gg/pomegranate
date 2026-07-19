package org.unstabledev.pomegranate

import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber

actual class File actual constructor(path: String) {
    actual fun createFile() {}
    actual fun createDirectory() {}
    actual fun readText(): String = ""
    actual fun writeText(text: String) {}
    actual fun readBytes(): ByteArray = ByteArray(0)
    actual fun writeBytes(bytes: ByteArray) {}
    actual fun delete() {}
    actual fun exists(): Boolean = false
    actual fun size(): Long { return 0 }

    actual companion object {
        actual val sep: String
            get() = ""
    }
}