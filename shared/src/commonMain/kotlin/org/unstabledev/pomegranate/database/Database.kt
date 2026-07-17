package org.unstabledev.pomegranate.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [ChatDC::class],
    version = 3,
)
@ConstructedBy(ChatDatabaseConstructor::class)
abstract class ChatDatabase: RoomDatabase() {
    abstract fun chatDao(): ChatDao
}

@Suppress("KotlinNoActualForExpect")
expect object ChatDatabaseConstructor : RoomDatabaseConstructor<ChatDatabase> {
    override fun initialize(): ChatDatabase
}
fun getChatDatabase(builder: RoomDatabase.Builder<ChatDatabase>): ChatDatabase{
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(true)
        .build()
}

@Database(
    entities = [MessageDC::class],
    version = 4,
)
@ConstructedBy(MessagesDatabaseConstructor::class)
abstract class MessagesDatabase: RoomDatabase() {
    abstract fun messagesDao(): MessagesDao
}

@Suppress("KotlinNoActualForExpect")
expect object MessagesDatabaseConstructor : RoomDatabaseConstructor<MessagesDatabase> {
    override fun initialize(): MessagesDatabase
}
fun getMessagesDatabase(builder: RoomDatabase.Builder<MessagesDatabase>): MessagesDatabase{
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(true)
        .build()
}