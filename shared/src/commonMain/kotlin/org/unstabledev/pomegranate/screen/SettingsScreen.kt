package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.AppSettings
import org.unstabledev.pomegranate.File
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.ThemeMode
import org.unstabledev.pomegranate.Util
import org.unstabledev.pomegranate.applyScreenPadding
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.isMobile

@Composable
fun SettingsScreen(navWayObj: NavigationWays, chatDao: ChatDao) {
    val settings by AppSettings.state.collectAsState()
    val scope = rememberCoroutineScope()

    Column(applyScreenPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                navWayObj.back()
                AppSettings.save()
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "Настройки",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp)) {
            Text("Внешний вид", fontWeight = FontWeight.SemiBold)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(32.dp)).height(64.dp)
            ) {
                Box(
                    Modifier.background(MaterialTheme.colorScheme.surface).fillMaxSize()
                ) {
                    Row(
                        Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { AppSettings.setTheme(ThemeMode.SYSTEM) }) {
                            Icon(
                                imageVector = Icons.Default.BrightnessAuto,
                                contentDescription = "Системная тема",
                                tint = if (settings.theme == ThemeMode.SYSTEM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.background,
                            thickness = 3.dp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        IconButton(onClick = { AppSettings.setTheme(ThemeMode.LIGHT) }) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = "Светлая тема",
                                tint = if (settings.theme == ThemeMode.LIGHT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.background,
                            thickness = 3.dp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        IconButton(onClick = { AppSettings.setTheme(ThemeMode.DARK) }) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = "Тёмная тема",
                                tint = if (settings.theme == ThemeMode.DARK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(settings.hideEmptyChats, { AppSettings.setHideEmptyChats(it) })
                Text("Скрывать пустые чаты")
            }
            Spacer(modifier = Modifier.padding(vertical = 10.dp))
            Text("Сеть", fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(settings.hideSendBarWhenNoNetwork, { AppSettings.setHideSendBarWhenNoNetwork(it) })
                Text("Отключать отправку без интернета")
            }
            Spacer(modifier = Modifier.padding(vertical = 5.dp))
            Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).fillMaxWidth().clickable {
                navWayObj.goTo("select_firebase")
            }) {
                Row(
                    Modifier.background(MaterialTheme.colorScheme.surface).fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.width(2.dp))
                    Text("Адрес Firebase")
                }
            }
            Spacer(modifier = Modifier.padding(vertical = 10.dp))
            Text("Хранилище и кэш", fontWeight = FontWeight.SemiBold)
            val chatCount = produceState(0) { value = chatDao.getAllChats().size }
            Text("Чатов: ${chatCount.value}")
            val chatCacheSize = remember { File("pomegranate${File.sep}chat.db").size() }
            Text("Размер БД чатов: ${Util.formatBinarySize(chatCacheSize)}")
            val chatMsgCacheSize = remember { File("pomegranate${File.sep}messages.db").size() }
            Text("Размер БД сообщений: ${Util.formatBinarySize(chatMsgCacheSize)}")
            Spacer(modifier = Modifier.padding(vertical = 5.dp))
            var showDeleteChatsPopup by remember { mutableStateOf(false) }
            Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).fillMaxWidth().clickable {
                showDeleteChatsPopup = true
            }) {
                Row(
                    Modifier.background(MaterialTheme.colorScheme.surface).fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(2.dp))
                    Text("Удалить все чаты", color = MaterialTheme.colorScheme.error)
                }
            }
            if (showDeleteChatsPopup) {
                val onDismiss = {showDeleteChatsPopup = false}
                AlertDialog(
                    onDismissRequest = onDismiss,
                    title = {
                        Text("Вы уверены что хотите удалить все чаты?")
                    },
                    text = {
                        Text("Это действие безвозвратно!")
                    },
                    confirmButton = {
                        Text("Подтвердить", Modifier.clickable {
                            scope.launch { chatDao.deleteAllChats() }
                            showDeleteChatsPopup = false
                        })
                    },
                    dismissButton = {
                        Text("Отмена", Modifier.clickable {
                            showDeleteChatsPopup = false
                        })
                    }
                )
            }
        }
    }
}
