package org.unstabledev.pomegranate.components

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownAnnotatorConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.painterResource
import org.unstabledev.pomegranate.AppSettings
import org.unstabledev.pomegranate.screen.control.HomeScreenController
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Util.Companion.stripMarkdown
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.deserialize
import pomegranate.shared.generated.resources.Res
import pomegranate.shared.generated.resources.menu

@Composable
fun SearchableChatsPanel(
    viewModel: HomeScreenController,
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

fun getLastMessageTextFlow(email: String): Flow<String> {
    return Repository.messagesDao.getLastMessageFlowByEmail(email)
        .map { msg ->
            if (msg != null) {
                val decodedText = try {
                    msg.data.decodeToString().stripMarkdown()
                } catch (e: Exception) {
                    ""
                }
                (if(msg.isMine) "Вы: " else "") + decodedText
            } else ""
        }
        .flowOn(Dispatchers.IO)
}

@Composable
fun addChatBackground(base: Modifier = Modifier): Modifier {
    return base.background(Brush.linearGradient(
        listOf(lerp(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.background, 0.25f), MaterialTheme.colorScheme.primary)
    ))
}

@Composable
fun ChatsList(chats: List<ChatDC>, onChatClick: (chat: ChatDC)->Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 5.dp)) {
        items(chats) { chat ->
            val message by getLastMessageTextFlow(chat.partnerEmail)
                .collectAsStateWithLifecycle(initialValue = "")
            val hasLast = message.isNotEmpty()
            if(!hasLast) return@items
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
                        ProfileImage(profile, chat)
                    }
                    Column(modifier = Modifier.fillMaxSize().padding(5.dp), verticalArrangement = Arrangement.Center) {
                        Text(
                            if(validProfile) profile.displayName else chat.partnerEmail,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if(hasLast) {
                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}