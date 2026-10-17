package org.unstabledev.pomegranate.database

import androidx.room.Room
import org.unstabledev.pomegranate.common.AppSettings
import java.io.File

private val link = AppSettings.state.value.currentLink

actual object DatabaseBuilder {
    actual fun chats(): ChatDatabase {
        val dbFile = File("${System.getProperty("user.dir")}${File.separator}pomegranate${File.separator}$link", "chat.db")
        return buildDatabase(Room.databaseBuilder<ChatDatabase>(name = dbFile.absolutePath))
    }

    actual fun messages(): MessagesDatabase {
        val dbFile = File("${System.getProperty("user.dir")}${File.separator}pomegranate${File.separator}$link", "messages.db")
        return buildDatabase(Room.databaseBuilder<MessagesDatabase>(name = dbFile.absolutePath))
    }

    actual fun persons(): PersonDatabase {
        val dbFile = File("${System.getProperty("user.dir")}${File.separator}pomegranate${File.separator}$link", "persons.db")
        return buildDatabase(Room.databaseBuilder<PersonDatabase>(name = dbFile.absolutePath))
    }
}