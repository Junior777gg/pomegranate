package org.unstabledev.pomegranate

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.unstabledev.pomegranate.database.ChatDatabase
import org.unstabledev.pomegranate.database.MessagesDatabase
import org.unstabledev.pomegranate.database.PersonDatabase

fun getChatDatabaseBuilder(context: Context): RoomDatabase.Builder<ChatDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("chat.db")
    return Room.databaseBuilder<ChatDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
fun getMessagesDatabaseBuilder(context: Context): RoomDatabase.Builder<MessagesDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("messages.db")
    return Room.databaseBuilder<MessagesDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

fun getPersonsDatabaseBuilder(context: Context): RoomDatabase.Builder<PersonDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("persons.db")
    return Room.databaseBuilder<PersonDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}