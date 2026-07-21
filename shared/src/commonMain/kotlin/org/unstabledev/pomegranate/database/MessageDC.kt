package org.unstabledev.pomegranate.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "messages")
data class MessageDC(
    @PrimaryKey(autoGenerate = true)
    val key: Long = 0,
    var email: String = "",
    var isDelivered: Boolean = false,
    val data: ByteArray,
    val type: String = "",
    val time: String = "",
    var isMine: Boolean = false,
){
    companion object{
        const val TEXT = "text"
        const val IMAGE = "image"
        const val FILE = "file"
    }
}
