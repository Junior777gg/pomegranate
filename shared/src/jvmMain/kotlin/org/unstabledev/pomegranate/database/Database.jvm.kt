package org.unstabledev.pomegranate.database

import androidx.room.Room
import org.unstabledev.pomegranate.common.AppSettings
import java.io.File


actual object DatabaseBuilder {
    actual fun chats(): ChatDatabase {
        val dbFile = File("${System.getProperty("user.dir")}${File.separator}pomegranate", "chat.db")
        return buildDatabase(Room.databaseBuilder<ChatDatabase>(name = dbFile.absolutePath))
    }

    actual fun messages(): MessagesDatabase {
        val dbFile = File("${System.getProperty("user.dir")}${File.separator}pomegranate", "messages.db")
        return buildDatabase(Room.databaseBuilder<MessagesDatabase>(name = dbFile.absolutePath))
    }

    actual fun persons(): PersonDatabase {
        val dbFile = File("${System.getProperty("user.dir")}${File.separator}pomegranate", "persons.db")
        return buildDatabase(Room.databaseBuilder<PersonDatabase>(name = dbFile.absolutePath))
    }
}