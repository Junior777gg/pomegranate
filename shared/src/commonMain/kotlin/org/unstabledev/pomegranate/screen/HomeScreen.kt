package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.SearchableChatsPanel
import org.unstabledev.pomegranate.File
import org.unstabledev.pomegranate.HomeScreenController
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Repository.fistFilePath
import org.unstabledev.pomegranate.Routes
import org.unstabledev.pomegranate.Util
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessagesDao

@Composable
fun HomeScreen(navWayObj: NavigationWays, chatDao: ChatDao, messagesDao: MessagesDao) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val viewModel = viewModel { HomeScreenController(chatDao) }

    val userEmail = "Гранат"
    val userName = Repository.myEmail

    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(16.dp)
                        .clickable {
                            Repository.lastOpponentEmail=Repository.myEmail
                            navWayObj.goTo(Routes.PROFILE_SCREEN_ROUTE)
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
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
                        Repository.lastOpponentEmail=Repository.myEmail
                        navWayObj.goTo(Routes.PROFILE_SCREEN_ROUTE)
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Настройки", fontSize = 16.sp) },
                    selected = false,
                    onClick = {
                        navWayObj.goTo(Routes.SETTINGS_SCREEN)
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Выйти", fontSize = 16.sp, color = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = {
                        File(fistFilePath).delete()
                        navWayObj.goTo(Routes.LOGIN_SCREEN)
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
        }
    ) {
        SearchableChatsPanel(viewModel, {
            Repository.setLastContact(it)
            navWayObj.goTo(Routes.CHAT_SCREEN)
        }, {
            navWayObj.goTo(Routes.CONTACTS_SCREEN)
        }, {
            scope.launch { drawerState.open() }
        })
    }
}