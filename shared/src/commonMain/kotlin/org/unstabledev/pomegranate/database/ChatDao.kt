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
    suspend fun insert(chat: ChatDC)

    @Upsert
    suspend fun upsert(chat: ChatDC)

    @Query("SELECT * FROM chat")
    fun getAll(): Flow<List<ChatDC>>

    @Query("SELECT * FROM chat WHERE chatName = :chatName LIMIT 1")
    fun getByName(chatName: String): Flow<ChatDC>

    @Query("SELECT * FROM chat WHERE `key` = :key LIMIT 1")
    suspend fun getByKey(key: Long): ChatDC

    @Query("SELECT * FROM chat WHERE chatName = :chatName LIMIT 1")
    fun tryGetByName(chatName: String): Flow<ChatDC?>

    @Query("SELECT EXISTS(SELECT 1 FROM chat WHERE chatName = :chatName AND chatCreator = :chatCreator AND chatType = :chatType LIMIT 1)")
    suspend fun exists(chatName: String, chatCreator: String, chatType: String): Boolean

    @Query("SELECT chatName FROM chat WHERE chatCreator = :chatCreator AND chatType = :chatType LIMIT 1")
    suspend fun getName(chatCreator: String, chatType: String): String

    @Query("DELETE FROM chat")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(chat: ChatDC)
}