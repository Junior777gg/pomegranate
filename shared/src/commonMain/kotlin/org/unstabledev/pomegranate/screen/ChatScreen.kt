package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.AppSettings
import org.unstabledev.pomegranate.ChooseFiles
import org.unstabledev.pomegranate.Firebase
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Routes
import org.unstabledev.pomegranate.Util
import org.unstabledev.pomegranate.applyScreenPadding
import org.unstabledev.pomegranate.components.ColorTheme
import org.unstabledev.pomegranate.components.ImagePreviewPanel
import org.unstabledev.pomegranate.components.MessageBubble
import org.unstabledev.pomegranate.components.NetworkWarningHeader
import org.unstabledev.pomegranate.components.ProfileImage
import org.unstabledev.pomegranate.components.ScrollToBottomButton
import org.unstabledev.pomegranate.components.addChatBackground
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.deserialize
import org.unstabledev.pomegranate.fileDropArea
import org.unstabledev.pomegranate.isMobile
import org.unstabledev.pomegranate.readBytes
import org.unstabledev.pomegranate.screen.control.ChatScreenController
import kotlin.time.Duration.Companion.seconds


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navWayObj: NavigationWays,
    chatDao: ChatDao,
    canBack: Boolean = true,
) {
    val lastContact by Repository.lastContact.collectAsState()
    val messagesDao = Repository.messagesDao
    val viewModel = viewModel(key = lastContact?.partnerEmail) {
        ChatScreenController(messagesDao, chatDao, lastContact!!)
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val inputState = rememberTextFieldState()
    val listState = rememberLazyListState()
    val messages = viewModel.messages.collectAsState()
    val chat by viewModel.chatDC.collectAsState()
    val settings by AppSettings.state.collectAsState()

    val isOnline by produceState(initialValue = true) {
        while (isActive) {
            value = Firebase.isAvailable()
            if (value) {
                delay(10.seconds)
            } else {
                delay(4.seconds)
            }
        }
    }
    val displayNewContactWidget = remember { mutableStateOf(true) }
    var showClearChatPopup by remember { mutableStateOf(false) }
    var showDeleteChatPopup by remember { mutableStateOf(false) }
    var showNicknameEditPopup by remember { mutableStateOf(false) }
    val messagePreview = remember { mutableStateOf<MessageDC?>(null) }
    val areFilesBeingDraggedOver = remember { mutableStateOf(false) }

    LaunchedEffect(listState, messages.value.size) {
        try {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastVisibleIndex ->
                    if (lastVisibleIndex != null && lastVisibleIndex >= messages.value.size - 5) {
                        viewModel.loadMore()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val messageCount = messages.value.size
        if (messageCount > 0) {
            listState.animateScrollToItem(0)
        }
    }

    val onImagePreviewClick: (MessageDC) -> Unit = remember {
        { msg -> messagePreview.value = msg }
    }
    Scaffold(
        modifier = applyScreenPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        if (messagePreview.value != null) {
            ImagePreviewPanel({ messagePreview.value = null }, messagePreview.value, snackbarHostState)
        } else {
            Box(modifier = addChatBackground(Modifier.fillMaxSize()).fileDropArea({ dropped ->
                println("got drag-and-drop event")
                areFilesBeingDraggedOver.value = false
                scope.launch {
                    try {
                        val preparedFiles: List<Pair<ByteArray, String>> = dropped.map { file ->
                            val fileBytes = file.readBytes()
                            Pair(fileBytes, file.name)
                        }

                        println("processed ${preparedFiles.size} dropped-in files")
                        viewModel.send(files = preparedFiles)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }, { areFilesBeingDraggedOver.value = true }, { areFilesBeingDraggedOver.value = false })) {
                Column {
                    val back = {
                        if (messages.value.isEmpty()) scope.launch {
                            chatDao.deleteChat(chat)
                            messagesDao.deleteAllByEmail(chat.partnerEmail)
                        }
                        navWayObj.goTo(Routes.HOME_SCREEN)
                    }
                    ChatHeader(
                        chat,
                        if (canBack) back else null,
                        {
                            Repository.lastOpponentEmail = chat.partnerEmail
                            navWayObj.goTo(Routes.PROFILE_SCREEN_ROUTE)
                        },
                        {
                            scope.launch {
                                listState.scrollToItem(messages.value.size - 1)
                            }
                        },
                        {
                            showClearChatPopup = true
                        },
                        {
                            showNicknameEditPopup = true
                        },
                        {
                            showDeleteChatPopup = true
                        }
                    )
                    NetworkWarningHeader()
                    LazyColumn(
                        state = listState,
                        reverseLayout = true,
                        modifier = addChatBackground(Modifier.fillMaxSize()),
                        contentPadding = PaddingValues(
                            top = 10.dp,
                            bottom = 86.dp,
                            start = 8.dp,
                            end = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(messages.value, key = { message -> message.key }) { message ->
                            MessageBubble(message, onImagePreviewClick, scope, snackbarHostState)
                        }
                        if (displayNewContactWidget.value) {
                            item {
                                NewContactWidget(chat = chat)
                            }
                        }
                    }
                }

                if (messages.value.isNotEmpty()) {
                    ScrollToBottomButton(
                        listState = listState,
                        messagesSize = messages.value.size,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = if (isMobile) 75.dp else 64.dp, end = 8.dp)
                    )
                }

                if (isOnline || !settings.hideSendBarWhenNoNetwork) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        MessageInput(
                            state = inputState,
                            viewModel
                        )
                    }
                }
            }

            if (areFilesBeingDraggedOver.value) {
                Column(Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.3f)),
                    verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Отпустите, чтобы отправить файлы")
                }
            }

            if (showClearChatPopup) {
                AlertDialog(
                    onDismissRequest = { showClearChatPopup = false },
                    title = {
                        Text(
                            "Вы уверены что хотите очистить чат?",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    text = {
                        Text("Это действие безвозвратно!")
                    },
                    confirmButton = {
                        Text("Подтвердить", Modifier.clickable {
                            scope.launch {
                                Repository.messagesDao.deleteAllByEmail(chat.partnerEmail)
                            }
                            showClearChatPopup = false
                        })
                    },
                    dismissButton = {
                        Text("Отмена", Modifier.clickable {
                            showClearChatPopup = false
                        })
                    }
                )
            }
            if (showDeleteChatPopup) {
                AlertDialog(
                    onDismissRequest = { showDeleteChatPopup = false },
                    title = {
                        Text(
                            "Вы уверены что хотите удалить чат?",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    text = {
                        Text("Это действие безвозвратно!")
                    },
                    confirmButton = {
                        Text("Подтвердить", Modifier.clickable {
                            scope.launch {
                                Repository.messagesDao.deleteAllByEmail(chat.partnerEmail)
                                chatDao.deleteChat(chat)
                            }
                            Repository.lastOpponentEmail = ""
                            Repository.setLastContact(null)
                            if (isMobile) navWayObj.goTo(Routes.HOME_SCREEN)
                            showDeleteChatPopup = false
                        })
                    },
                    dismissButton = {
                        Text("Отмена", Modifier.clickable {
                            showDeleteChatPopup = false
                        })
                    }
                )
            }
            if (showNicknameEditPopup) {
                val newNicknameState = rememberTextFieldState()
                AlertDialog(
                    onDismissRequest = { showNicknameEditPopup = false },
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
                                val updatedChat = chat.copy(nickname = newNicknameState.text.toString().takeIf { it.isNotBlank() })
                                chatDao.upsertChat(updatedChat)
                            }
                            showNicknameEditPopup = false
                        })
                    },
                    dismissButton = {
                        Text("Отмена", Modifier.clickable {
                            showNicknameEditPopup = false
                        })
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatHeader(
    chat: ChatDC,
    onBackClick: (() -> Unit)?,
    onProfileClick: () -> Unit,
    onScrollToTopClick: () -> Unit,
    onClearHistoryClick: () -> Unit,
    onNicknameEditClick: () -> Unit,
    onDeleteChatClick: () -> Unit,
) {
    val profile = chat.profile?.deserialize()
    val validProfile = profile?.profileUrl?.isNotBlank() ?: false
    val menuExpanded = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .height(56.dp)
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
            ProfileImage(profile, chat)

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

        Box {
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

@Composable
private fun MessageInput(
    state: TextFieldState,
    viewModel: ChatScreenController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp, vertical = if (isMobile) {
                    if (Util.isKeyboardVisible()) 48.dp else 25.dp
                } else 10.dp
            ),
        verticalAlignment = Alignment.Bottom
    ) {
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 40.dp, max = 120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row {
                if (state.text.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .height(22.5.dp)
                            .clickable {
                                ChooseFiles().getFiles { files ->
                                    if (files.isNotEmpty()) {
                                        viewModel.send(files = files)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Attachment,
                            contentDescription = "Отправить",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.5.dp).rotate(315.0f)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                }
                BasicTextField(
                    state = state,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(ColorTheme.MessageAccent),
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 4),
                    decorator = { innerTextField ->
                        if (state.text.isEmpty()) {
                            Text(
                                text = "Сообщение",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(ColorTheme.MessageAccent)
                .clickable {
                    val text = state.text.toString().trim()
                    if (text.isNotEmpty()) {
                        viewModel.send(text)
                        state.clearText()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Отправить",
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun NewContactWidget(
    chat: ChatDC
) {
    val profile = chat.profile?.deserialize()
    val displayName = if (profile?.displayName?.isNotBlank() == true) {
        profile.displayName
    } else {
        chat.partnerEmail
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val validProfile = profile?.profileUrl?.isNotBlank() ?: false

            Text(
                text = displayName,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Новый контакт",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Страна",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (validProfile) {
                        if (profile.location.isNotEmpty()) "${countryFlags[profile.location] ?: "🌍"} ${profile.location}"
                        else "🏴‍☠️ Аноним"
                    } else "🏴‍☠️ Аноним",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            /*Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Первое сообщение",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${firstMessage.time}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }*/

            Spacer(modifier = Modifier.width(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Предупреждение",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Не официальный аккаунт",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

private val countryFlags = mapOf(
    "Russia" to "🇷🇺",
    "Ukraine" to "🇺🇦",
    "Belarus" to "🇧🇾",
    "Kazakhstan" to "🇰🇿",
    "USA" to "🇺🇸",
    "Canada" to "🇨🇦",
    "Mexico" to "🇲🇽",
    "Brazil" to "🇧🇷",
    "Argentina" to "🇦🇷",
    "UK" to "🇬🇧",
    "Germany" to "🇩🇪",
    "France" to "🇫🇷",
    "Italy" to "🇮🇹",
    "Spain" to "🇪🇸",
    "Portugal" to "🇵🇹",
    "Netherlands" to "🇳🇱",
    "Belgium" to "🇧🇪",
    "Switzerland" to "🇨🇭",
    "Austria" to "🇦🇹",
    "Poland" to "🇵🇱",
    "Czech Republic" to "🇨🇿",
    "Slovakia" to "🇸🇰",
    "Hungary" to "🇭🇺",
    "Romania" to "🇷🇴",
    "Bulgaria" to "🇧🇬",
    "Serbia" to "🇷🇸",
    "Croatia" to "🇭🇷",
    "Greece" to "🇬🇷",
    "Turkey" to "🇹🇷",
    "China" to "🇨🇳",
    "Japan" to "🇯🇵",
    "South Korea" to "🇰🇷",
    "India" to "🇮🇳",
    "Israel" to "🇮🇱",
    "Saudi Arabia" to "🇸🇦",
    "UAE" to "🇦🇪",
    "Australia" to "🇦🇺",
    "New Zealand" to "🇳🇿",
    "South Africa" to "🇿🇦",
    "Nigeria" to "🇳🇬",
    "Egypt" to "🇪🇬"
)

