package org.unstabledev.pomegranate.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Insert
    suspend fun insert(person: PersonDC)

    @Upsert
    suspend fun upsert(person: PersonDC)

    @Query("SELECT * FROM persons")
    fun getAll(): Flow<List<PersonDC>>

    @Query("SELECT * FROM persons WHERE personEmail = :email LIMIT 1")
    suspend fun getByEmail(email: String): PersonDC?

    @Query("SELECT * FROM persons WHERE personEmail = :email LIMIT 1")
    fun tryGetByEmail(email: String): Flow<PersonDC?>

    @Query("DELETE FROM persons")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(person: PersonDC)
}