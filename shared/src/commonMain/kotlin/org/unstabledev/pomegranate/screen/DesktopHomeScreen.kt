package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.unstabledev.pomegranate.File
import org.unstabledev.pomegranate.MainScreenController
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Repository.fistFilePath
import org.unstabledev.pomegranate.Routes
import org.unstabledev.pomegranate.SearchableChatsPanel
import org.unstabledev.pomegranate.Util
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessagesDao

private enum class PanelSunScreen {
    CHATS,
    CONTACTS,
    PROFILE_SETTINGS,
}

@Composable
fun DesktopHomeScreen(navWayObj: NavigationWays, chatDao: ChatDao, messagesDao: MessagesDao) {
    var panelSubScreen by remember { mutableStateOf(PanelSunScreen.CHATS) }
    val lastContact by Repository.lastContact.collectAsState()

    val viewModel = viewModel { MainScreenController(chatDao, messagesDao) }
    val chats by viewModel.chats.collectAsState()

    val userEmail = "Гранат"
    val userName = Repository.myEmail

    Row {
        Column(Modifier.weight(1f)) {
            when (panelSubScreen) {
                PanelSunScreen.CHATS -> {
                    SearchableChatsPanel(
                        viewModel,
                        onChatClick = {
                            Repository.setLastContact(it to Repository.availableChats[it])
                        },
                        onChatAddClick = {
                            panelSubScreen = PanelSunScreen.CONTACTS
                        },
                        onSidemenuClick = {
                            panelSubScreen = PanelSunScreen.PROFILE_SETTINGS
                        })
                }
                PanelSunScreen.CONTACTS -> {
                    ContactsPanel({
                        panelSubScreen = PanelSunScreen.CHATS
                    }, {
                        panelSubScreen = PanelSunScreen.CHATS
                    }, chatDao, messagesDao)
                }
                PanelSunScreen.PROFILE_SETTINGS -> {
                    ProfileSettings(userEmail, userName, {
                        panelSubScreen = PanelSunScreen.CHATS
                    }, {
                        Repository.lastOpponentEmail = Repository.myEmail
                        navWayObj.goTo(Routes.PROFILE_SCREEN_ROUTE)
                    }, {
                        navWayObj.goTo(Routes.SETTINGS_SCREEN)
                    }, {
                        File(fistFilePath).delete()
                        navWayObj.goTo(Routes.LOGIN_SCREEN)
                    })
                }
            }
        }
        Column(Modifier.weight(1.5f)) {
            if(lastContact?.first?.partnerEmail?.isNotEmpty() == true) {
                key(lastContact?.first?.partnerEmail) {
                    ChatScreen(
                        navWayObj = navWayObj,
                        messagesDao = messagesDao,
                        chatDC = lastContact!!.first,
                        observer = lastContact!!.second
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSettings(userEmail: String, userName: String,
    onBack: ()->Unit, onProfileClick: ()->Unit, onSettingsClick: ()->Unit, onLogOut: ()->Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .height(54.dp)
            .padding(horizontal=4.dp).padding(top=16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onBack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(start=16.dp, end=16.dp, bottom=16.dp, top=0.dp)
            .clickable {
                onProfileClick()
            }
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(Util.randomColor(userEmail.hashCode(), isSystemInDarkTheme())),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.take(1).uppercase(),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = userName,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = userEmail,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    NavigationDrawerItem(
        label = { Text("Профиль", fontSize = 16.sp) },
        selected = false,
        onClick = {
            onProfileClick()
        },
        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )

    NavigationDrawerItem(
        label = { Text("Настройки", fontSize = 16.sp) },
        selected = false,
        onClick = {
            onSettingsClick()
        },
        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )

    NavigationDrawerItem(
        label = { Text("Выйти", fontSize = 16.sp, color = MaterialTheme.colorScheme.error) },
        selected = false,
        onClick = {
            onLogOut()
        },
        icon = {
            Icon(
                Icons.Default.ExitToApp,
                contentDescription = "Logout",
                tint = MaterialTheme.colorScheme.error
            )
        },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}