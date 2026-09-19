package org.unstabledev.pomegranate.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kotlincrypto.hash.sha2.SHA256
import org.unstabledev.pomegranate.screen.Profile

class ChatConverters {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else json.decodeFromString(value)
    }

    @TypeConverter
    fun fromStringStringMap(value: Map<String, String?>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringStringMap(value: String): Map<String, String?> {
        return if (value.isEmpty()) emptyMap() else json.decodeFromString(value)
    }

    @TypeConverter
    fun fromStringBooleanMap(value: Map<String, Boolean?>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringBooleanMap(value: String): Map<String, Boolean?> {
        return if (value.isEmpty()) emptyMap() else json.decodeFromString(value)
    }
}

@Serializable
@TypeConverters(ChatConverters::class)
@Entity(tableName = "chat")
data class ChatDC(
    val name: String? = null,
    val partnerEmail: List<String>,
    val nickname: Map<String, String?> = emptyMap(),
    var profile: Map<String, String?> = emptyMap(),
    var securityConfig: Map<String, String?> = emptyMap(),
    val knownContact: Map<String, Boolean?> = emptyMap(),
    @PrimaryKey(autoGenerate = true)
    val key: Long = 0,
){
}
fun Profile.serialize(): String {
    return Json.encodeToString(this)
}
fun String.deserialize(): Profile{
    return Json.decodeFromString(Profile.serializer(), this)
}
fun String.sha256(): String {
    val hash = SHA256().digest(this.encodeToByteArray())
    return hash.toHexString()
}
fun String.decodeFromSha256(): String{
    return this.hexToByteArray().decodeToString()
}
