package org.unstabledev.pomegranate.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface MessagesDao {
    @Insert
    suspend fun insertMessage(message: MessageDC)

    @Upsert
    suspend fun upsertMessage(message: MessageDC)

    @Query("SELECT * FROM messages WHERE email = :email")
    suspend fun getAllByEmail(email: String): List<MessageDC>

    @Query("DELETE FROM messages WHERE email = :email")
    suspend fun deleteAllByEmail(email: String)

    @Query("SELECT * FROM messages WHERE data = :data")
    suspend fun getByData(data: ByteArray) : MessageDC

    @Query("SELECT * FROM messages WHERE email = :email ORDER BY `key` DESC LIMIT 1")
    suspend fun getLastByEmail(email: String): MessageDC

    @Delete
    suspend fun deleteMessage(message: MessageDC)
}