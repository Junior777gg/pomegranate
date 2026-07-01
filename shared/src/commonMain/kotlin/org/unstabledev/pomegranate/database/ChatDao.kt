package org.unstabledev.pomegranate.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ChatDao {
    @Insert
    suspend fun insertChat(chat: ChatDC)

    @Upsert
    suspend fun upsertChat(chat: ChatDC)

    @Query("SELECT * FROM chat")
    suspend fun getAllChats(): List<ChatDC>

    @Query("DELETE FROM chat")
    suspend fun deleteAllChats()

    @Delete
    suspend fun deleteChat(chat: ChatDC)

}