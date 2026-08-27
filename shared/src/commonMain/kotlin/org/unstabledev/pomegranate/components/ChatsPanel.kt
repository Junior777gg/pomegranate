package org.unstabledev.pomegranate.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.unstabledev.pomegranate.AppSettings
import org.unstabledev.pomegranate.BackgroundStorage
import org.unstabledev.pomegranate.ChatBackgroundIds
import org.unstabledev.pomegranate.HAPTIC_EFFECT_CLICK
import org.unstabledev.pomegranate.screen.control.HomeScreenController
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Util.Companion.stripMarkdown
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.deserialize
import org.unstabledev.pomegranate.getBitmapFromBytes
import org.unstabledev.pomegranate.kmpReadBytes
import org.unstabledev.pomegranate.sendHaptic
import pomegranate.shared.generated.resources.Res
import pomegranate.shared.generated.resources.def01
import pomegranate.shared.generated.resources.def02
import pomegranate.shared.generated.resources.def03
import pomegranate.shared.generated.resources.def04
import pomegranate.shared.generated.resources.def05
import pomegranate.shared.generated.resources.def06
import pomegranate.shared.generated.resources.def07
import pomegranate.shared.generated.resources.def08
import pomegranate.shared.generated.resources.menu
import pomegranate.shared.generated.resources.welcome_mobile

@Composable
fun SearchableChatsPanel(
    viewModel: HomeScreenController,
    onChatClick: (chat: ChatDC) -> Unit,
    onChatAddClick: () -> Unit,
    onSidemenuClick: () -> Unit,
    onOpenProfileClick: (chat: ChatDC) -> Unit,
    chatDao: ChatDao,
    modifier: Modifier = Modifier
) {
    val chats by viewModel.chats.collectAsState()
    val sufColor = MaterialTheme.colorScheme.surface

    val searchState = rememberTextFieldState()
    val searchText = searchState.text.toString().trim()

    val filteredChats = if (searchText.isEmpty()) {
        chats
    } else {
        chats.filter { chat ->
            chat.partnerEmail.contains(searchText, ignoreCase = true)
        }
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                        .clickable { onSidemenuClick() },
                    painter = painterResource(Res.drawable.menu),
                    contentDescription = "menu",
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onChatAddClick() },
                    imageVector = Icons.Default.Add,
                    contentDescription = "add",
                    tint = MaterialTheme.colorScheme.onBackground
                )
                LabeledTextField(searchState, "Поиск", singleLineIn = true)
            }
        }
        NetworkWarningHeader()
        if (filteredChats.isNotEmpty()) {
            ChatsList(filteredChats, onChatClick, onOpenProfileClick, chatDao)
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    if (chats.isEmpty()) "Вы еще ни с кем не общались! Заведите новый чат."
                    else "Ничего не найдено",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

fun getLastMessageTextFlow(email: String): Flow<String> {
    return Repository.messagesDao.getLastMessageFlowByEmail(email)
        .map { msg ->
            if (msg != null) {
                val decodedText = try {
                    when (msg.type) {
                        MessageDC.TEXT -> msg.data.decodeToString().stripMarkdown()
                        MessageDC.IMAGE -> "Изображение"
                        MessageDC.ANIMATED_IMAGE -> "Изображение"
                        MessageDC.AUDIO -> "Аудио"
                        MessageDC.FILE -> "Файл"
                        MessageDC.BEGIN_CALL -> "Начался звонок"
                        MessageDC.ACCEPT_CALL -> "Принят звонок"
                        else -> "Неизвестно"
                    }
                } catch (_: Exception) {
                    ""
                }

                (if (msg.isMine) "Вы: " else "") + decodedText
            } else ""
        }
        .flowOn(Dispatchers.IO)
}

@Composable
fun addChatBackground(base: Modifier = Modifier): Modifier {
    val settings by AppSettings.state.collectAsState()

    return when (settings.chatBackgroundId) {
        ChatBackgroundIds.DEFAULT_PRIMARY -> addChatBackground_defPrimary(base)

        ChatBackgroundIds.CUSTOM -> {
            val customBgFile = BackgroundStorage.getCustomBackgroundFile()
            if (customBgFile.exists()) {
                try {
                    val bitmap = getBitmapFromBytes(customBgFile.kmpReadBytes())
                    base.paint(
                        painter = BitmapPainter(bitmap),
                        contentScale = ContentScale.Crop
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    addChatBackground_defPrimary(base)
                }
            } else {
                addChatBackground_defPrimary(base)
            }
        }

        else -> addChatBackground_defImage(settings.chatBackgroundId, base)
    }
}

@Composable
fun addChatBackground_defPrimary(base: Modifier = Modifier): Modifier {
    return base.background(
        Brush.linearGradient(
            listOf(
                lerp(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.background,
                    0.25f
                ),
                MaterialTheme.colorScheme.primary
            )
        )
    )
}
@Composable
fun addChatBackground_defImage(id: Int, base: Modifier = Modifier): Modifier {
    return base.paint(
        painter = painterResource(
            when (id) {
                1->Res.drawable.def01
                2->Res.drawable.def02
                3->Res.drawable.def03
                4->Res.drawable.def04
                5->Res.drawable.def05
                6->Res.drawable.def06
                7->Res.drawable.def07
                8->Res.drawable.def08
                else->Res.drawable.def01
            }
        ),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ChatsList(chats: List<ChatDC>, onChatClick: (chat: ChatDC) -> Unit, onOpenProfileClick: (chat: ChatDC) -> Unit, chatDao: ChatDao) {
    val scope = rememberCoroutineScope()
    val selectedChat = remember { mutableStateOf<ChatDC?>(null) }
    val showNicknameEditPopup = remember { mutableStateOf(false) }
    val settings by AppSettings.state.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 5.dp)) {
        items(items = chats, key = { it.partnerEmail }) { chat ->
            val menuExpanded = remember { mutableStateOf(false) }
            val message by getLastMessageTextFlow(chat.partnerEmail)
                .collectAsStateWithLifecycle(initialValue = "")
            val hasLast = message.isNotEmpty()
            //if (!hasLast) return@items
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                menuExpanded.value = true
                                sendHaptic(HAPTIC_EFFECT_CLICK)
                            },
                            onTap = { onChatClick(chat) }
                        )
                    }
            ) {
                val profile = chat.profile?.deserialize()
                val validProfile = profile?.profileUrl?.isNotBlank() ?: false
                Row(modifier = Modifier.fillMaxWidth().height(64.dp)) {
                    Column(
                        modifier = Modifier.width(64.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProfileImage(profile, chat)
                    }
                    Column(modifier = Modifier.fillMaxSize().padding(5.dp), verticalArrangement = Arrangement.Center) {
                        val displayName = chat.nickname?:(if (validProfile) profile.displayName else chat.partnerEmail)
                        Text(
                            displayName,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (hasLast) {
                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                maxLines = if(settings.chatTripleColumn) 2 else 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                DropdownMenu(
                    expanded = menuExpanded.value,
                    onDismissRequest = { menuExpanded.value = false },
                    modifier = Modifier.width(230.dp).background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("Профиль", color = MaterialTheme.colorScheme.onBackground)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            onOpenProfileClick(chat)
                            menuExpanded.value = false
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text("Изменить никнейм", color = MaterialTheme.colorScheme.onBackground)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuExpanded.value = false
                            showNicknameEditPopup.value = true
                            selectedChat.value = chat
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text("Удалить", color = MaterialTheme.colorScheme.error)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                tint = MaterialTheme.colorScheme.error,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            scope.launch {
                                Repository.messagesDao.deleteAllByEmail(chat.partnerEmail)
                                chatDao.deleteChat(chat)
                            }
                            menuExpanded.value = false
                        }
                    )
                }
            }
        }
    }
    if (showNicknameEditPopup.value&&selectedChat.value!=null) {
        val newNicknameState = rememberTextFieldState()
        AlertDialog(
            onDismissRequest = { showNicknameEditPopup.value = false },
            title = {
                Text(
                    "Изменить никнейм",
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                TextField(newNicknameState)
            },
            confirmButton = {
                Text("Подтвердить", Modifier.clickable {
                    scope.launch {
                        val updatedChat = selectedChat.value!!.copy(nickname = newNicknameState.text.toString().takeIf { it.isNotBlank() })
                        chatDao.upsertChat(updatedChat)
                        selectedChat.value=updatedChat
                    }
                    showNicknameEditPopup.value = false
                })
            },
            dismissButton = {
                Text("Отмена", Modifier.clickable {
                    showNicknameEditPopup.value = false
                })
            }
        )
    }
}