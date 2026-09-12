package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.AppSettings
import org.unstabledev.pomegranate.Firebase
import org.unstabledev.pomegranate.KMPFile
import org.unstabledev.pomegranate.screen.nav.NavigationWays
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.screen.nav.Routes
import org.unstabledev.pomegranate.components.ImagePreviewPanel
import org.unstabledev.pomegranate.components.chat.MessageBubble
import org.unstabledev.pomegranate.components.chat.MessageInput
import org.unstabledev.pomegranate.components.NetworkWarningHeader
import org.unstabledev.pomegranate.components.chat.NewContactWidget
import org.unstabledev.pomegranate.components.ScrollToBottomButton
import org.unstabledev.pomegranate.components.chat.ChatHeader
import org.unstabledev.pomegranate.components.chat.addChatBackground
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.fileDropArea
import org.unstabledev.pomegranate.isMobile
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
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= messages.value.size - 5)
                    viewModel.loadMore()
            }
    }
    LaunchedEffect(messages.value.size) {
        if (messages.value.isNotEmpty()) listState.animateScrollToItem(0)
    }

    val onImagePreviewClick: (MessageDC) -> Unit = remember {
        { msg -> messagePreview.value = msg }
    }
    if (messagePreview.value == null && isMobile) {
        Box(Modifier.fillMaxSize().padding(top=100.dp)) {
            Box(addChatBackground().fillMaxSize())
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        if (messagePreview.value != null) {
            ImagePreviewPanel({ messagePreview.value = null }, messagePreview.value, snackbarHostState)
        } else {
            var m = Modifier.fillMaxSize().fileDropArea({ dropped ->
                println("got drag-and-drop event")
                areFilesBeingDraggedOver.value = false
                scope.launch {
                    try {
                        val preparedFiles: List<KMPFile> = dropped as List<KMPFile>
                        println("processed ${preparedFiles.size} dropped-in files")
                        viewModel.send(files = preparedFiles, type = MessageDC.FILE)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }, { areFilesBeingDraggedOver.value = true }, { areFilesBeingDraggedOver.value = false })
            if (!isMobile) m = addChatBackground(m);
            Box(modifier = m) {
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
                        viewModel,
                        if (canBack) back else null,
                        {
                            viewModel.send(message = null, type = MessageDC.BEGIN_CALL)
                        },
                        {
                            viewModel.send(message = null, type = MessageDC.BEGIN_CALL)
                        },
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
                            MessageBubble(message, chat, onImagePreviewClick,
                                scope, snackbarHostState, settings.parseMarkdown)
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
                        MessageInput(inputState, viewModel, scope)
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
