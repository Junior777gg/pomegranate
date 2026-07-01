package org.unstabledev.pomegranate

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessagesDao

@Composable
fun App(chatDao: ChatDao, messagesDao: MessagesDao) {
    MaterialTheme {
        Scaffold(Modifier.fillMaxSize().padding(vertical = 30.dp)) {
            val navController = rememberNavController()
             Navigation(navController, chatDao)
        }
    }
}