package org.unstabledev.pomegranate.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ChatConverters {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromStringList(value: MutableList<String>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): MutableList<String> {
        return if (value.isEmpty()) mutableListOf() else json.decodeFromString(value)
    }
}

@Serializable
@TypeConverters(ChatConverters::class)
@Entity(tableName = "chat")
data class ChatDC(
    val chatName: String,
    val chatType: String,
    val chatCreator: String,
    val personsEmails: MutableList<String>,
    @PrimaryKey(autoGenerate = true)
    val key: Long = 0,
) {
    companion object {
        object ChatTypes {
            const val SELFCHAT = "SELFCHAT"
            const val CHAT = "chat"
            const val GROUP = "group"
        }
    }
}
