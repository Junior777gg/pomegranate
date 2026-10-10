package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.unstabledev.pomegranate.common.AppSettings
import org.unstabledev.pomegranate.Firebase
import org.unstabledev.pomegranate.common.Util.Companion.toLocalDateTime
import org.unstabledev.pomegranate.common.Util.Companion.toTimeMark
import org.unstabledev.pomegranate.platform.KMPFile
import org.unstabledev.pomegranate.screen.nav.Routes
import org.unstabledev.pomegranate.components.chat.MessageBubble
import org.unstabledev.pomegranate.components.chat.MessageInput
import org.unstabledev.pomegranate.components.NetworkWarningHeader
import org.unstabledev.pomegranate.components.ScrollToBottomButton
import org.unstabledev.pomegranate.components.chat.ChatHeader
import org.unstabledev.pomegranate.components.chat.NewContactWidget
import org.unstabledev.pomegranate.components.chat.addChatBackground
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.platform.fileDropArea
import org.unstabledev.pomegranate.platform.isMobile
import org.unstabledev.pomegranate.screen.control.ChatScreenController
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    chatKey: Long,
    canBack: Boolean = true,
    onChatDelete: () -> Unit,
) {
    val viewModel = viewModel(key = chatKey.toString()) {
        ChatScreenController(chatKey, onChatDelete)
    }
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    val inputState = rememberTextFieldState()
    val listState = rememberLazyListState()
    val messages = viewModel.messages.collectAsState()
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
    val areFilesBeingDraggedOver = remember { mutableStateOf(false) }

    @Composable
    fun TimeMark(time: String) {
        Column(
            Modifier.fillMaxWidth().height(37.dp).padding(vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                Modifier.weight(1f)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(vertical = 5.dp, horizontal = 15.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(time, color = Color.White)
            }
        }
    }

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

    if (isMobile) {
        Box(Modifier.fillMaxSize().padding(top = 100.dp)) {
            Box(addChatBackground().fillMaxSize())
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
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
            } }, { areFilesBeingDraggedOver.value = true }, { areFilesBeingDraggedOver.value = false })
        if (!isMobile) m = addChatBackground(m)
        Box(modifier = m) {
            Column {
                var back: (() -> Unit)? = null
                if (canBack) back = {
                    if (messages.value.isEmpty() &&
                        viewModel.getChat().chatType != ChatDC.Companion.ChatTypes.GROUP) scope.launch { viewModel.deleteChat()
                        viewModel.deleteMessages() }
                    navController.navigate(Routes.HomeScreen())
                }
                ChatHeader(
                    viewModel,
                    back,
                    {
                        viewModel.send(message = null, type = MessageDC.BEGIN_CALL, files = listOf())
                    },
                    {
                        viewModel.send(message = null, type = MessageDC.BEGIN_CALL, files = listOf())
                    },
                    {
                        if(viewModel.getChat().chatType==ChatDC.Companion.ChatTypes.GROUP) navController.navigate(Routes.ProfileScreen(chatKey, null))
                        else navController.navigate(Routes.ProfileScreen(chatKey, viewModel.getChat().personsEmails.first()))
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
                        val msgTime = message.time.toLocalDateTime()
                        MessageBubble(
                            message, { navController.navigate(Routes.ImagePreview(message.key)) },
                            scope, snackBarHostState, settings.parseMarkdown
                        )

                        val currentIndex = messages.value.indexOf(message)
                        if (currentIndex == messages.value.size - 1) {
                            TimeMark(msgTime.toTimeMark())
                        } else {
                            val nextMessage = messages.value[currentIndex + 1]
                            val nextMsgTime = nextMessage.time.toLocalDateTime()

                            if (msgTime.day != nextMsgTime.day ||
                                msgTime.month != nextMsgTime.month ||
                                msgTime.year != nextMsgTime.year) {
                                TimeMark(msgTime.toTimeMark())
                            }
                        }
                    }
                    if (displayNewContactWidget.value) {
                        item {
                            NewContactWidget(viewModel)
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
            Column(
                Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.3f)),
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    ) },
                text = {
                    Text("Это действие безвозвратно!") },
                confirmButton = {
                    Text("Подтвердить", Modifier.clickable {
                        scope.launch {
                            viewModel.deleteMessages()
                        }
                        showClearChatPopup = false
                    }) },
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
                        "Вы уверены что хотите ${if(viewModel.getChat().chatType==ChatDC.Companion.ChatTypes.GROUP) "покинуть" else "удалить"} чат?",
                        color = MaterialTheme.colorScheme.onBackground
                    ) },
                text = {
                    Text("Это действие безвозвратно!") },
                confirmButton = {
                    Text("Подтвердить", Modifier.clickable {
                        scope.launch {
                            viewModel.deleteMessages()
                            viewModel.deleteChat()
                        }
                        if (isMobile) navController.navigate(route = Routes.HomeScreen())
                        showDeleteChatPopup = false
                    }) },
                dismissButton = {
                    Text("Отмена", Modifier.clickable {
                        showDeleteChatPopup = false
                    })
                }
            )
        }
        if (showNicknameEditPopup) {
            val newNameState = rememberTextFieldState()
            AlertDialog(
                onDismissRequest = { showNicknameEditPopup = false },
                title = {
                    Text(
                        "Изменить никнейм",
                        color = MaterialTheme.colorScheme.onBackground
                    ) },
                text = {
                    TextField(newNameState) },
                confirmButton = {
                    Text("Подтвердить", Modifier.clickable {
                        scope.launch {
                            viewModel.renameChat(newNameState.text.toString().takeIf { it.isNotBlank() })
                        }
                        showNicknameEditPopup = false
                    }) },
                dismissButton = {
                    Text("Отмена", Modifier.clickable {
                        showNicknameEditPopup = false
                    })
                }
            )
        }
    }
}
