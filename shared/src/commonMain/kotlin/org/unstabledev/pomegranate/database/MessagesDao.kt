package org.unstabledev.pomegranate.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {
    @Insert
    suspend fun insertMessage(message: MessageDC)

    @Upsert
    suspend fun upsertMessage(message: MessageDC)

    @Query("SELECT * FROM messages WHERE chatName = :name AND chatCreator = :creator AND chatType = :chatType ORDER BY `time` DESC LIMIT 1")
    fun getLastMessage(name: String, creator: String, chatType: String): Flow<MessageDC>

    @Query("DELETE FROM messages WHERE chatName = :name AND chatCreator = :creator AND chatType = :chatType")
    suspend fun deleteAll(name: String, creator: String, chatType: String)

    @Query("SELECT * FROM messages WHERE chatName = :name AND chatCreator = :creator AND chatType = :chatType ORDER BY `time` DESC LIMIT :limit")
    fun getPaged(name: String, creator: String, chatType: String, limit: Int): Flow<List<MessageDC>>

    @Query("SELECT EXISTS(SELECT 1 FROM messages WHERE chatName = :name AND type = :type ORDER BY `time` LIMIT 1)")
    fun hasMessagesOfType(name: String, type: String): Flow<Boolean>

    @Query("SELECT * FROM messages WHERE data = :data")
    suspend fun getByData(data: ByteArray) : MessageDC?

    @Query("SELECT * FROM messages WHERE chatName = :name ORDER BY `time` DESC LIMIT 1")
    suspend fun getLastByName(name: String): MessageDC?

    @Query("SELECT * FROM messages WHERE chatName = :name ORDER BY `time` DESC LIMIT 1")
    fun getLastMessageFlowByName(name: String): Flow<MessageDC?>

    @Delete
    suspend fun deleteMessage(message: MessageDC)
}