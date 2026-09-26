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

    @Query("SELECT * FROM chat WHERE chatName = :chatName LIMIT 1")
    fun getChatByNameFlow(chatName: String): Flow<ChatDC>

    @Query("SELECT * FROM chat WHERE chatName = :chatName LIMIT 1")
    fun tryGetChatByNameFlow(chatName: String): Flow<ChatDC?>

    @Query("SELECT EXISTS(SELECT 1 FROM chat WHERE chatName = :chatName AND chatCreator = :chatCreator AND chatType = :chatType LIMIT 1)")
    suspend fun isThisChatExists(chatName: String, chatCreator: String, chatType: String): Boolean

    @Query("SELECT chatName FROM chat WHERE chatCreator = :chatCreator AND chatType = :chatType LIMIT 1")
    suspend fun getChatName(chatCreator: String, chatType: String): String

    @Query("DELETE FROM chat")
    suspend fun deleteAllChats()

    @Delete
    suspend fun deleteChat(chat: ChatDC)
}