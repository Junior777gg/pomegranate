package org.unstabledev.pomegranate.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert
    suspend fun insertChat(chat: ChatDC)

    @Upsert
    suspend fun upsertChat(chat: ChatDC)

    @Query("SELECT * FROM chat")
    fun getAllChatsFlow(): Flow<List<ChatDC>>

    @Query("SELECT * FROM chat WHERE partnerEmail = :email LIMIT 1")
    fun getChatByEmailFlow(email: String): Flow<ChatDC>

    @Query("DELETE FROM chat")
    suspend fun deleteAllChats()

    @Delete
    suspend fun deleteChat(chat: ChatDC)
}