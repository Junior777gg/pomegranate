package org.unstabledev.pomegranate.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageDC(
    @PrimaryKey val email: String,
    val data: ByteArray,
    val type: String,
    val time: String,
    val isMine: Boolean
){
    companion object{
        const val TEXT = "text"
    }
}
