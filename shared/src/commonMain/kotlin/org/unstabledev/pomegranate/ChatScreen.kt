package org.unstabledev.pomegranate

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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import org.unstabledev.pomegranate.database.MessageDC


private object ChatColors {
    val Background = Color(0xFFE7EBF0)
    val MyBubble = Color(0xFFE3FFC7)
    val OtherBubble = Color(0xFFFFFFFF)
    val HeaderBackground = Color(0xFFFFFFFF)
    val InputBackground = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF000000)
    val TextSecondary = Color(0xFF707579)
    val SendButton = Color(0xFF3390EC)
    val ReadCheck = Color(0xFF3390EC)
}

@Composable
fun ChatScreen(
    navWayObj: NavigationWays,
) {
    val viewModel = viewModel { ChatScreenController() }
    val inputState = rememberTextFieldState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val messages = viewModel.messages.collectAsState()


    LaunchedEffect(messages.value.size) {
        if (messages.value.isNotEmpty()) {
            listState.animateScrollToItem(messages.value.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(ChatColors.Background)) {
        ChatHeader(
            partnerName = "",
            onBackClick = { navWayObj.goTo(Routes.HOME_SCREEN_ROUTE) }
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(messages.value, key = { it.email }) { message ->
                MessageBubble(message)
            }
        }

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

@Composable
private fun ChatHeader(
    partnerName: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(ChatColors.HeaderBackground)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = ChatColors.TextPrimary
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF5C6BC0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = partnerName.take(1).uppercase(),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = partnerName,
                color = ChatColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        IconButton(onClick = { /* TODO */ }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Меню",
                tint = ChatColors.TextPrimary
            )
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
                .widthIn(max = 280.dp)  // максимальная ширина пузыря
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isMine) 16.dp else 4.dp,
                        bottomEnd = if (message.isMine) 4.dp else 16.dp
                    )
                )
                .background(if (message.isMine) ChatColors.MyBubble else ChatColors.OtherBubble)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = message.data.decodeToString(),
                    color = ChatColors.TextPrimary,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.time,
                        color = ChatColors.TextSecondary,
                        fontSize = 11.sp
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
            .background(ChatColors.InputBackground)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 40.dp, max = 120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF0F2F5))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = ChatColors.TextPrimary,
                    fontSize = 15.sp
                ),
                cursorBrush = SolidColor(ChatColors.SendButton),
                lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 4),
                decorator = { innerTextField ->
                    if (state.text.isEmpty()) {
                        Text(
                            text = "Сообщение",
                            color = ChatColors.TextSecondary,
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
                .background(ChatColors.SendButton)
                .clickable { onSend() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Отправить",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
