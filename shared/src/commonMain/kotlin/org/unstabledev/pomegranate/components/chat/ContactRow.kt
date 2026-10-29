package org.unstabledev.pomegranate.components.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.painterResource
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.components.ProfileImage
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.PersonDC
import org.unstabledev.pomegranate.database.deserialize
import pomegranate.shared.generated.resources.Res
import pomegranate.shared.generated.resources.pin

@Composable
fun ContactRow(chat: ChatDC, hasLast: Boolean = false, message: String = "", messageTime: String = "",
               tripleRow: Boolean = false, isPinned: Boolean = false, useEmoji: Boolean = false, modifier: Modifier = Modifier) {
    val personDC = runBlocking { Repository.personsDao.getByEmail(chat.personsEmails[0]) }
    val profile = personDC?.profile?.deserialize()
    val name = when (chat.chatType) {
        ChatDC.Companion.ChatTypes.CHAT -> {
            personDC?.nickname ?: profile?.displayName ?: chat.personsEmails[0]
        }
        ChatDC.Companion.ChatTypes.GROUP -> {
            chat.chatName
        }
        else -> ""
    }
    ContactRow(name, hasLast, message, messageTime, tripleRow, isPinned, modifier) {
        ProfileImage(chat, useEmoji = useEmoji)
    }
}
@Composable
fun ContactRow(email: String, hasLast: Boolean = false, message: String = "", messageTime: String = "",
               tripleRow: Boolean = false, isPinned: Boolean = false, useEmoji: Boolean = false, modifier: Modifier = Modifier) {
    val personDC = runBlocking { Repository.personsDao.getByEmail(email) }
    val profile = personDC?.profile?.deserialize()
    val name = personDC?.nickname ?: profile?.displayName ?: email
    ContactRow(name, hasLast, message, messageTime, tripleRow, isPinned, modifier) {
        ProfileImage(personDC, email, useEmoji = useEmoji)
    }
}
@Composable
fun ContactRow(personDC: PersonDC, hasLast: Boolean = false, message: String = "", messageTime: String = "",
               tripleRow: Boolean = false, isPinned: Boolean = false, useEmoji: Boolean = false, modifier: Modifier = Modifier) {
    val profile = personDC.profile?.deserialize()
    val name = personDC.nickname ?: profile?.displayName ?: ""
    ContactRow(name, hasLast, message, messageTime, tripleRow, isPinned, modifier) {
        ProfileImage(personDC, personDC.personEmail, useEmoji = useEmoji)
    }
}

@Composable
fun ContactRow(name: String, hasLast: Boolean, message: String,
               messageTime: String, tripleRow: Boolean, isPinned: Boolean,
               modifier: Modifier, pfp: @Composable (ColumnScope.() -> Unit)) {
    Row(modifier = modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier.width(64.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            pfp()
        }
        Column(Modifier.weight(1f).padding(start=5.dp), verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, modifier = Modifier.weight(1f), maxLines = 1, color = MaterialTheme.colorScheme.onBackground)
                Row(Modifier.padding(end = 5.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End) {
                    Text(messageTime, color = MaterialTheme.colorScheme.onSurface,
                        autoSize = TextAutoSize.StepBased(8.sp, 12.sp))
                    if (isPinned) {
                        Spacer(Modifier.width(5.dp))
                        Icon(
                            painter = painterResource(Res.drawable.pin),
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp).rotate(45.0f)
                        )
                    }
                }
            }
            if (hasLast) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        maxLines = if(tripleRow) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}