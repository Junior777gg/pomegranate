package org.unstabledev.pomegranate.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kotlincrypto.hash.sha2.SHA256
import org.unstabledev.pomegranate.screen.Profile

@Serializable
@Entity(tableName = "chat")
data class ChatDC(
    @PrimaryKey val partnerEmail: String,
    val profile: String?
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
