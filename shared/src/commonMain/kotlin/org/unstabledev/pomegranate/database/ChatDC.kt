package org.unstabledev.pomegranate.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.kotlincrypto.hash.sha2.SHA256

@Serializable
@Entity(tableName = "chat")
data class ChatDC(
    @PrimaryKey val partnerEmail: String,
)
/*
fun String.sha256(): String {
    val hash = SHA256().digest(this.encodeToByteArray())
    return hash.toHexString()
}
fun String.decodeFromSha256(): String{
    return this.hexToByteArray().decodeToString()
}*/
