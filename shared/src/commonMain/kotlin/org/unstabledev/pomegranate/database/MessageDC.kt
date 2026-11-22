package org.unstabledev.pomegranate.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.unstabledev.pomegranate.common.Util.Companion.stripMarkdown

@Serializable
@Entity(tableName = "messages")
data class MessageDC(
    var chatName: String,
    var chatType: String,
    var chatCreator: String = "",
    val messageCreator: String = "",
    var data: ByteArray,
    var supData: ByteArray = ByteArray(0),
    var isDelivered: Boolean = false,
    val type: String = "",
    val time: Long = 0,
    var isMine: Boolean = false,
    var instructions: String = "",
    var bulkId: Long = 0,
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
        const val BEGIN_CALL = "call:begin"
        const val ACCEPT_CALL = "call:accept"
        const val END_CALL = "call:end"
        const val INVITE = "invite"
        const val BUTTON = "button"

        fun MessageDC.isDisplayable(): Boolean {
            return type!=SECURITY_CONFIG
        }
        fun MessageDC.canDisplaySender(): Boolean {
            return type!=INVITE && type!=BEGIN_CALL && type!=ACCEPT_CALL && type!=END_CALL && type!=SECURITY_CONFIG
        }

        fun MessageDC.description(): String {
            return when (this.type) {
                TEXT -> this.data.decodeToString()
                    .stripMarkdown()

                BEGIN_CALL, ACCEPT_CALL -> "📞 Звонок"
                IMAGE, ANIMATED_IMAGE -> "🖼 Изображение"
                AUDIO -> "🎵 Аудио"
                FILE -> "📁 Файл"
                else -> "Неизвестно"
            }
        }

        fun truncatedMessageDCType(type: String): String? {
            return when (type) {
                TEXT -> "dat"
                IMAGE, ANIMATED_IMAGE -> "img"
                FILE, AUDIO -> "file"
                else -> null
            }
        }
    }
}

