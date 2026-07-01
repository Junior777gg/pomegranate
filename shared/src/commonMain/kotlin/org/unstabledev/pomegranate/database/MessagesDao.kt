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

    @Delete
    suspend fun deleteMessage(message: MessageDC)
}