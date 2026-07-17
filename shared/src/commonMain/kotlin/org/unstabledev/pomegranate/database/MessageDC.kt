package org.unstabledev.pomegranate.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageDC(
    @PrimaryKey(autoGenerate = true)
    val key: Long = 0,
    val email: String,
    var isDelivered: Boolean = false,
    val data: ByteArray,
    val type: String,
    val time: String,
    val isMine: Boolean
){
    companion object{
        const val TEXT = "text"
    }
}
