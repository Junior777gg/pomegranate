package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.components.addChatBackground
import org.unstabledev.pomegranate.AppSettings
import org.unstabledev.pomegranate.isMobile
import org.unstabledev.pomegranate.screen.control.ChatScreenController
import org.unstabledev.pomegranate.Firebase
import org.unstabledev.pomegranate.components.GeneratedProfileImage
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.components.NetworkWarningHeader
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Routes
import org.unstabledev.pomegranate.Util
import org.unstabledev.pomegranate.components.ProfileImage
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.deserialize
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


private object ChatColors {
    val MyBubble = Color(0xFF8BFF1A)
    val Accent = Color(0xFF3390EC)
}

@Composable
fun ChatScreen(
    navWayObj: NavigationWays,
    canBack: Boolean = true,
) {
    val lastContact by Repository.lastContact.collectAsState()
    val messagesDao = Repository.messagesDao
    // Create ViewModel with the specific chat
    val viewModel = viewModel(key = lastContact?.partnerEmail) {
        ChatScreenController(messagesDao, lastContact!!)
    }
    val scope = rememberCoroutineScope()

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

    LaunchedEffect(messages.value.size) {
        if (messages.value.isNotEmpty()) {
            listState.animateScrollToItem(messages.value.size - 1)
        }
    }

    val displayNewContactWidget = true

    Column(modifier = addChatBackground(Modifier.fillMaxSize())) {
        val back = {navWayObj.goTo(Routes.HOME_SCREEN)}
        ChatHeader(
            chat,
            if(canBack) back else null,
            {
                Repository.lastOpponentEmail = chat.partnerEmail
                navWayObj.goTo(Routes.PROFILE_SCREEN_ROUTE)
            },
            {
                scope.launch {
                    Repository.messagesDao.deleteAllByEmail(chat.partnerEmail)
                }
            },
            {
                scope.launch {
                    Repository.messagesDao.deleteAllByEmail(chat.partnerEmail)
                }
                navWayObj.goTo(Routes.HOME_SCREEN)
            }
        )
        NetworkWarningHeader()

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if(displayNewContactWidget) {
                item {
                    NewContactWidget(chat = chat)
                }
            }
            items(messages.value) { message ->
                MessageBubble(message)
            }
        }

        if(isOnline || !settings.hideSendBarWhenNoNetwork) {
            MessageInput(
                state = inputState,
                onSend = {
                    val text = inputState.text.toString().trim()
                    if (text.isNotEmpty()) {
                        viewModel.send(text)
                        inputState.clearText()
                    }
                }
            )
        }
    }
}

@Composable
private fun ChatHeader(
    chat: ChatDC,
    onBackClick: (() -> Unit)?,
    onProfileClick: () -> Unit,
    onClearHistoryClick: () -> Unit,
    onDeleteChatClick: () -> Unit,
) {
    val profile = chat.profile?.deserialize()
    val validProfile = profile?.profileUrl?.isNotBlank() ?: false
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(onBackClick!=null) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Row(Modifier.clickable(indication = null, interactionSource = null) { onProfileClick() }.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            ProfileImage(profile, chat)

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = if (validProfile) profile.displayName else chat.partnerEmail,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Меню",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
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
                        menuExpanded = false
                        onProfileClick()
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
                        menuExpanded = false
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
                        menuExpanded = false
                        onDeleteChatClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MessageDC) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isMine) 16.dp else 4.dp,
                        bottomEnd = if (message.isMine) 4.dp else 16.dp
                    )
                )
                .background(if (message.isMine) ChatColors.MyBubble else MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = message.data.decodeToString(),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.time,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        modifier = Modifier.size(15.dp),
                        imageVector = if(message.isDelivered||!message.isMine) Icons.Default.Check else Icons.Default.ArrowOutward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageInput(
    state: TextFieldState,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp, vertical = if (isMobile) {
                    if (Util.isKeyboardVisible()) 48.dp else 25.dp
                } else 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 40.dp, max = 120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp
                ),
                cursorBrush = SolidColor(ChatColors.Accent),
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

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(ChatColors.Accent)
                .clickable { onSend() },
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
                    text = if(validProfile) {
                        if (profile.location.isNotEmpty()) "${countryFlags[profile.location]?:"🌍"} ${profile.location}"
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

