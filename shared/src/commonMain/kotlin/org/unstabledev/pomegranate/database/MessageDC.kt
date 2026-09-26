package org.unstabledev.pomegranate.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "messages")
data class MessageDC(
    var chatName: String,
    var chatType: String,
    var chatCreator: String = "",
    val messageCreator: String = "",
    var data: ByteArray,
    var isDelivered: Boolean = false,
    val type: String = "",
    val time: Long = 0,
    var isMine: Boolean = false,
    var instructions: String = "",
    @PrimaryKey(autoGenerate = true)
    val key: Long = 0,
){
    companion object{
        const val TEXT = "text"
        const val IMAGE = "image"
        const val ANIMATED_IMAGE = "anim_img"
        const val FILE = "file"
        const val AUDIO = "audio"
        const val SECURITY_CONFIG = "security:config"
        const val BEGIN_CALL = "call:begin_video"
        const val ACCEPT_CALL = "call:accept"

        fun MessageDC.isCall(): Boolean {
            return type==BEGIN_CALL ||
                    type==ACCEPT_CALL
        }
        fun MessageDC.isDisplayable(): Boolean {
            return type!=SECURITY_CONFIG
        }
    }
}

