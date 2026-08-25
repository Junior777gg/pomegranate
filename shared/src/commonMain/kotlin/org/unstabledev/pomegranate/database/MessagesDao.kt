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

    @Query("SELECT * FROM messages WHERE email = :email ORDER BY `time`")
    fun getAllByEmail(email: String): Flow<List<MessageDC>>

    @Query("SELECT * FROM messages WHERE email = :email AND type = :type ORDER BY `time`")
    fun getAllOfTypeByEmail(email: String, type: String): Flow<List<MessageDC>>

    @Query("SELECT EXISTS(SELECT 1 FROM messages WHERE email = :email AND type = :type ORDER BY `time` LIMIT 1)")
    fun hasMessagesOfType(email: String, type: String): Flow<Boolean>

    @Query("SELECT * FROM messages WHERE email = :email ORDER BY `time` DESC LIMIT :limit")
    fun getPagedByEmail(email: String, limit: Int): Flow<List<MessageDC>>

    @Query("DELETE FROM messages WHERE email = :email")
    suspend fun deleteAllByEmail(email: String)

    @Query("SELECT * FROM messages WHERE data = :data")
    suspend fun getByData(data: ByteArray) : MessageDC?

    @Query("SELECT * FROM messages WHERE email = :email ORDER BY `time` DESC LIMIT 1")
    suspend fun getLastByEmail(email: String): MessageDC?

    @Query("SELECT * FROM messages WHERE email = :email ORDER BY `time` DESC LIMIT 1")
    fun getLastMessageFlowByEmail(email: String): Flow<MessageDC?>

    @Delete
    suspend fun deleteMessage(message: MessageDC)
}