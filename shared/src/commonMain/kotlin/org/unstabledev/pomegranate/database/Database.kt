package org.unstabledev.pomegranate.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [ChatDC::class], version = 16)
@ConstructedBy(ChatDatabaseConstructor::class)
abstract class ChatDatabase: RoomDatabase() {
    abstract fun get(): ChatDao
}

@Suppress("KotlinNoActualForExpect")
expect object ChatDatabaseConstructor : RoomDatabaseConstructor<ChatDatabase> {
    override fun initialize(): ChatDatabase
}

@Database(entities = [MessageDC::class], version = 21)
@ConstructedBy(MessagesDatabaseConstructor::class)
abstract class MessagesDatabase: RoomDatabase() {
    abstract fun get(): MessagesDao
}

@Suppress("KotlinNoActualForExpect")
expect object MessagesDatabaseConstructor : RoomDatabaseConstructor<MessagesDatabase> {
    override fun initialize(): MessagesDatabase
}

@Database(entities = [PersonDC::class], version = 1)
@ConstructedBy(PersonDatabaseConstructor::class)
abstract class PersonDatabase: RoomDatabase() {
    abstract fun get(): PersonDao
}

@Suppress("KotlinNoActualForExpect")
expect object PersonDatabaseConstructor : RoomDatabaseConstructor<PersonDatabase> {
    override fun initialize(): PersonDatabase
}

expect object DatabaseBuilder {
    fun chats(): ChatDatabase
    fun messages(): MessagesDatabase
    fun persons(): PersonDatabase
}

fun <T : RoomDatabase> buildDatabase(builder: RoomDatabase.Builder<T>): T {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(true)
        .build()
}
