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
    var data: ByteArray,
    val type: String = "",
    val time: String = "",
    var isMine: Boolean = false,
){
    companion object{
        const val TEXT = "text"
        const val IMAGE = "image"
        const val ANIMATED_IMAGE = "anim_img"
        const val FILE = "file"
        const val AUDIO = "audio"
        const val BEGIN_CALL = "call:begin_video"
        const val ACCEPT_CALL = "call:accept"
    }
}

