package org.unstabledev.pomegranate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.compose.rememberNavController
import org.unstabledev.pomegranate.api.event.PublicEventHandler
import org.unstabledev.pomegranate.common.AppSettings
import org.unstabledev.pomegranate.common.DraftManager
import org.unstabledev.pomegranate.components.ColorTheme
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.DatabaseBuilder
import org.unstabledev.pomegranate.database.MessagesDao
import org.unstabledev.pomegranate.database.PersonDao
import org.unstabledev.pomegranate.platform.setStatusBarIcons
import org.unstabledev.pomegranate.screen.nav.Navigation

@Composable
fun App() {
    val firstBoot = remember { mutableStateOf(true) }
    val theme = ColorTheme()
    val settings by AppSettings.state.collectAsState()
    setStatusBarIcons(AppSettings.isLightTheme(settings))
    if (firstBoot.value) {
        DraftManager.load()
        PublicEventHandler.fetch()
        firstBoot.value=false
    }
    val chatDao = DatabaseBuilder.chats().get()
    val messagesDao = DatabaseBuilder.messages().get()
    val personsDao = DatabaseBuilder.persons().get()
    var color: Color? = null
    if (settings.primaryColor!=ColorTheme.AccentDark.toArgb()) color=Color(settings.primaryColor)

    theme.AppTheme(theme = settings.theme, useAmoledOnDarkSystem = settings.useAmoledOnDarkSystem, primaryColor = color) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize())
        Scaffold(Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            Navigation(navController, chatDao, messagesDao, personsDao)
        }
    }
}