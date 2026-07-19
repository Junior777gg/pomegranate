package org.unstabledev.pomegranate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.unstabledev.pomegranate.components.ColorTheme
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessagesDao

@Composable
fun App(chatDao: ChatDao, messagesDao: MessagesDao) {
    val theme = ColorTheme()
    val settings by AppSettings.state.collectAsState()

    LaunchedEffect(Unit) {
        AppSettings.load()
    }

    setStatusBarIcons(AppSettings.isLightTheme(settings))

    theme.AppTheme(theme = settings.theme) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize())
        Scaffold(Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            Navigation(navController, chatDao, messagesDao)
        }
    }
}