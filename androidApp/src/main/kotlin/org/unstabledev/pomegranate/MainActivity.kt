package org.unstabledev.pomegranate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.unstabledev.pomegranate.database.getChatDatabase
import org.unstabledev.pomegranate.database.getMessagesDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        File.context = this
        super.onCreate(savedInstanceState)
        val chatBuilder = getChatDatabaseBuilder(applicationContext)
        val chatDatabase = getChatDatabase(chatBuilder)
        val chatDao = chatDatabase.chatDao()
        val messagesBuilder = getMessagesDatabaseBuilder(applicationContext)
        val messagesDatabase = getMessagesDatabase(messagesBuilder)
        val messagesDao = messagesDatabase.messagesDao()
        setContent{
            App(chatDao, messagesDao)
        }
    }
}