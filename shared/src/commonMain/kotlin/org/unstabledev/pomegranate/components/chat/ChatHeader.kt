package org.unstabledev.pomegranate.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.components.ProfileImage
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.deserialize
import org.unstabledev.pomegranate.screen.control.ChatScreenController

@Composable
fun ChatHeader(
    chat: ChatDC,
    viewModel: ChatScreenController,
    onBackClick: (() -> Unit)?,
    onAudioCallClick: (() -> Unit),
    onVideoCallClick: (() -> Unit),
    onProfileClick: () -> Unit,
    onScrollToTopClick: () -> Unit,
    onClearHistoryClick: () -> Unit,
    onNicknameEditClick: () -> Unit,
    onDeleteChatClick: () -> Unit,
) {
    val profile = chat.profile?.deserialize()
    val validProfile = profile?.profileUrl?.isNotBlank() ?: false
    val menuExpanded = remember { mutableStateOf(false) }
    val isOnline = produceState(false) {value = Repository.isChatOpen(chat)}

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .height(56.dp)
            .zIndex(2.0f)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Row(
            Modifier.clickable(indication = null, interactionSource = null) { onProfileClick() }.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileImage(profile, chat, isOnline = isOnline.value)

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                val displayName = chat.nickname?:(if (validProfile) profile.displayName else chat.partnerEmail)
                Text(
                    text = displayName,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row {
            IconButton(onClick = { onAudioCallClick() }) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Аудио звонок",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(onClick = { onVideoCallClick() }) {
                Icon(
                    imageVector = Icons.Default.VideoCall,
                    contentDescription = "Видео звонок",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(onClick = { menuExpanded.value = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Меню",
                    tint = MaterialTheme.colorScheme.onBackground
                )
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
                        menuExpanded.value = false
                        onProfileClick()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text("В начало", color = MaterialTheme.colorScheme.onBackground)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardDoubleArrowUp,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded.value = false
                        onScrollToTopClick()
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
                        onNicknameEditClick()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text("Очистить историю", color = MaterialTheme.colorScheme.onBackground)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded.value = false
                        onClearHistoryClick()
                    }
                )

                HorizontalDivider()

                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Удалить чат",
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        menuExpanded.value = false
                        onDeleteChatClick()
                    }
                )
            }
        }
    }
}