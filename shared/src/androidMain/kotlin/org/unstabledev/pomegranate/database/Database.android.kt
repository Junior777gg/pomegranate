package org.unstabledev.pomegranate.database

import androidx.room.Room
import org.unstabledev.pomegranate.common.AppSettings
import org.unstabledev.pomegranate.platform.androidContext


actual object DatabaseBuilder {
    actual fun chats(): ChatDatabase {
        val appContext = androidContext!!.applicationContext
        val dbFile = appContext.getDatabasePath("chat.db")
        return buildDatabase(Room.databaseBuilder<ChatDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        ))
    }
    actual fun messages(): MessagesDatabase {
        val appContext = androidContext!!.applicationContext
        val dbFile = appContext.getDatabasePath("messages.db")
        return buildDatabase(Room.databaseBuilder<MessagesDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        ))
    }
    actual fun persons(): PersonDatabase {
        val appContext = androidContext!!.applicationContext
        val dbFile = appContext.getDatabasePath("persons.db")
        return buildDatabase(Room.databaseBuilder<PersonDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        ))
    }
}