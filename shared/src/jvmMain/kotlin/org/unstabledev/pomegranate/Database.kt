package org.unstabledev.pomegranate

import androidx.room.Room
import androidx.room.RoomDatabase
import org.unstabledev.pomegranate.common.AppSettings
import org.unstabledev.pomegranate.database.ChatDatabase
import org.unstabledev.pomegranate.database.MessagesDatabase
import org.unstabledev.pomegranate.database.PersonDatabase
import java.io.File

private val link = AppSettings.state.value.currentLink
fun getChatDatabaseBuilder(): RoomDatabase.Builder<ChatDatabase> {
    val dbFile = File("${System.getProperty("user.dir")}${File.separator}pomegranate${File.separator}$link", "chat.db")
    return Room.databaseBuilder<ChatDatabase>(
        name = dbFile.absolutePath,
    )
}

fun getMessagesDatabaseBuilder(): RoomDatabase.Builder<MessagesDatabase> {
    val dbFile = File("${System.getProperty("user.dir")}${File.separator}pomegranate${File.separator}$link", "messages.db")
    return Room.databaseBuilder<MessagesDatabase>(
        name = dbFile.absolutePath,
    )
}

fun getPersonsDatabaseBuilder(): RoomDatabase.Builder<PersonDatabase> {
    val dbFile = File("${System.getProperty("user.dir")}${File.separator}pomegranate${File.separator}$link", "persons.db")
    return Room.databaseBuilder<PersonDatabase>(
        name = dbFile.absolutePath,
    )
}