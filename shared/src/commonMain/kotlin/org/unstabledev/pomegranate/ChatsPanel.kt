package org.unstabledev.pomegranate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.deserialize
import pomegranate.shared.generated.resources.Res
import pomegranate.shared.generated.resources.menu

@Composable
fun SearchableChatsPanel(
    viewModel: MainScreenController,
    onChatClick: (chat: ChatDC)->Unit,
    onChatAddClick: ()->Unit,
    onSidemenuClick: ()->Unit,
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
            ChatsList(filteredChats, onChatClick)
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

@Composable
fun ChatsList(chats: List<ChatDC>, onChatClick: (chat: ChatDC)->Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 5.dp)) {
        items(chats) { chat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clickable {
                        onChatClick(chat)
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
}