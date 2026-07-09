package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalWifiConnectedNoInternet4
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.unstabledev.pomegranate.File
import org.unstabledev.pomegranate.Firebase
import org.unstabledev.pomegranate.GeneratedProfileImage
import org.unstabledev.pomegranate.MainScreenController
import org.unstabledev.pomegranate.LabeledTextField
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.NetworkWarningHeader
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Repository.fistFilePath
import org.unstabledev.pomegranate.Routes
import org.unstabledev.pomegranate.Util
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessagesDao
import org.unstabledev.pomegranate.database.deserialize
import pomegranate.shared.generated.resources.Res
import pomegranate.shared.generated.resources.menu

@Composable
fun HomeScreen(navWayObj: NavigationWays, chatDao: ChatDao, messagesDao: MessagesDao) {
    val viewModel = viewModel { MainScreenController(chatDao, messagesDao) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.update()
    }

    val chats = viewModel.chats.collectAsState()
    val sufColor = MaterialTheme.colorScheme.surface
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val userEmail = "Гранат"
    val userName = Repository.myEmail

    val searchState = rememberTextFieldState()
    val searchText = searchState.text.toString().trim()

    val filteredChats = if (searchText.isEmpty()) {
        chats.value
    } else {
        chats.value.filter { chat ->
            chat.partnerEmail.contains(searchText, ignoreCase = true)
        }
    }

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
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(
                modifier = Modifier
                    .height(50.dp).fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = sufColor,
                            start = Offset(x = 0f, y = size.height),
                            end = Offset(x = size.width, y = size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(start = 5.dp, end = 5.dp, top = 0.dp)
            ) {
                Row(
                    modifier = Modifier.height(50.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .size(26.dp)
                            .clickable {
                                scope.launch { drawerState.open() }
                            },
                        painter = painterResource(Res.drawable.menu),
                        contentDescription = "menu",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Icon(
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { navWayObj.goTo(Routes.CONTACTS_SCREEN) },
                        imageVector = Icons.Default.Add,
                        contentDescription = "add",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    LabeledTextField(searchState, "Поиск", singleLineIn = true)
                }
            }
            NetworkWarningHeader()
            if (filteredChats.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 5.dp)) {
                    items(filteredChats) { chat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clickable {
                                    Repository.lastContact = chat to Repository.availableChats[chat]
                                    navWayObj.goTo(Routes.CHAT_SCREEN)
                                }
                        ) {
                            val partnerName = chat.partnerEmail
                            val profile = chat.profile?.deserialize()
                            val validProfile = profile?.profileUrl?.isNotBlank() ?: false
                            Row(modifier = Modifier.fillMaxWidth().height(64.dp)) {
                                    Column(
                                        modifier = Modifier.width(64.dp).fillMaxHeight(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (validProfile) {
                                            AsyncImage(
                                                model = profile.avatarUrl,
                                                contentDescription = profile.displayName,
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .clip(CircleShape)
                                            )
                                        } else GeneratedProfileImage(partnerName, size=60.dp)
                                    }
                                Column(modifier = Modifier.fillMaxSize().padding(5.dp)) {
                                    Text(if(validProfile) profile.displayName else chat.partnerEmail, color = MaterialTheme.colorScheme.onBackground)
                                    Text(
                                        text = "<последнее сообщение>",
                                        style = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        if (chats.value.isEmpty()) "Вы еще ни с кем не общались! Заведите новый чат."
                        else "Ничего не найдено",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}