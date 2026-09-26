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
    suspend fun insertPerson(person: PersonDC)

    @Upsert
    suspend fun upsertPerson(person: PersonDC)

    @Query("SELECT * FROM persons")
    fun getAllPersonsFlow(): Flow<List<PersonDC>>

    @Query("SELECT * FROM persons WHERE personEmail = :email LIMIT 1")
    suspend fun getPersonByEmail(email: String): PersonDC?

    @Query("SELECT * FROM persons WHERE personEmail = :email LIMIT 1")
    fun tryGetPersonByEmailFlow(email: String): Flow<PersonDC?>

    @Query("DELETE FROM persons")
    suspend fun deleteAllPersons()

    @Delete
    suspend fun deleteChat(person: PersonDC)
}