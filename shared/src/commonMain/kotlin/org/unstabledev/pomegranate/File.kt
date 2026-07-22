package org.unstabledev.pomegranate

import androidx.compose.ui.graphics.ImageBitmap

expect class File(path: String){
    companion object{
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

expect class ChooseFiles(){
    fun getFiles(): List<Pair<ByteArray, String>>
}

expect fun getBitmapFromBytes(bytes: ByteArray): ImageBitmap