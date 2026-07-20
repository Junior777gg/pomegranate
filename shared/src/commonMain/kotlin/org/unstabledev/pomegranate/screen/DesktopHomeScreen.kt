package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.unstabledev.pomegranate.AppSettings
import org.unstabledev.pomegranate.File
import org.unstabledev.pomegranate.screen.control.HomeScreenController
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Repository.fistFilePath
import org.unstabledev.pomegranate.Routes
import org.unstabledev.pomegranate.components.SearchableChatsPanel
import org.unstabledev.pomegranate.Util
import org.unstabledev.pomegranate.components.addChatBackground
import org.unstabledev.pomegranate.database.ChatDao

private enum class PanelSubScreen {
    CHATS,
    CONTACTS,
    PROFILE_SETTINGS,
}

@Composable
fun DesktopHomeScreen(navWayObj: NavigationWays, chatDao: ChatDao) {
    var panelSubScreen by remember { mutableStateOf(PanelSubScreen.CHATS) }
    val lastContact by Repository.lastContact.collectAsState()
    val settings by AppSettings.state.collectAsState()
    val viewModel = viewModel { HomeScreenController(chatDao) }

    val userEmail = "Гранат"
    val userName = Repository.myEmail

    var splitPosition by remember { mutableFloatStateOf(settings.desktopHomeSplit) }
    val minLeftWidth = 0.5f
    val maxLeftWidth = 2.0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onKeyEvent { keyEvent ->
                if (keyEvent.key == Key.Escape && keyEvent.type == KeyEventType.KeyUp) {
                    if(panelSubScreen != PanelSubScreen.CHATS) {
                        panelSubScreen = PanelSubScreen.CHATS
                    } else {
                        Repository.setLastContact(null)
                    }
                    true
                } else {
                    false
                }
            }
    ) {
        Row {
            Column(Modifier.weight(splitPosition)) {
                when (panelSubScreen) {
                    PanelSubScreen.CHATS -> {
                        SearchableChatsPanel(
                            viewModel,
                            onChatClick = {
                                Repository.setLastContact(it)
                            },
                            onChatAddClick = {
                                panelSubScreen = PanelSubScreen.CONTACTS
                            },
                            onSidemenuClick = {
                                panelSubScreen = PanelSubScreen.PROFILE_SETTINGS
                            })
                    }
                    PanelSubScreen.CONTACTS -> {
                        ContactsPanel({
                            panelSubScreen = PanelSubScreen.CHATS
                        }, {
                            panelSubScreen = PanelSubScreen.CHATS
                        }, chatDao)
                    }
                    PanelSubScreen.PROFILE_SETTINGS -> {
                        ProfileSettings(userEmail, userName, {
                            panelSubScreen = PanelSubScreen.CHATS
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
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .pointerHoverIcon(PointerIcon.Crosshair)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { },
                            onDragEnd = { AppSettings.setDesktopHomeSplit(splitPosition) },
                            onDragCancel = { }
                        ) { change, dragAmount ->
                            change.consume()
                            val newPosition = (splitPosition*500 + dragAmount)/500
                            splitPosition = newPosition.coerceIn(minLeftWidth, maxLeftWidth)
                        }
                    }
                    .background(
                        MaterialTheme.colorScheme.surface
                    )
            )
            Column(Modifier.weight(1.5f)) {
                if(lastContact?.partnerEmail?.isNotEmpty() == true) {
                    key(lastContact?.partnerEmail) {
                        ChatScreen(
                            navWayObj = navWayObj,
                            chatDao = chatDao,
                            canBack = false
                        )
                    }
                } else {
                    Column(addChatBackground()) {
                        Box(Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSettings(userEmail: String, userName: String,
    onBack: ()->Unit, onProfileClick: ()->Unit, onSettingsClick: ()->Unit, onLogOut: ()->Unit, modifier: Modifier = Modifier
) {
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
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(start=16.dp, end=16.dp, bottom=16.dp, top=0.dp)
            .clickable(indication = null, interactionSource = null) {
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