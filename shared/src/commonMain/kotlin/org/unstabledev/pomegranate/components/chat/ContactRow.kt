package org.unstabledev.pomegranate.components.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.runBlocking
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.common.Util.Companion.toHHMMTime
import org.unstabledev.pomegranate.components.ProfileImage
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.PersonDC
import org.unstabledev.pomegranate.database.deserialize

@Composable
fun ContactRow(chat: ChatDC, hasLast: Boolean = false, message: String = "", messageTime: String = "",
               tripleColumn: Boolean = false) {
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
    ContactRow(name, hasLast, message, messageTime, tripleColumn) {
        ProfileImage(chat)
    }
}
@Composable
fun ContactRow(email: String, hasLast: Boolean = false, message: String = "", messageTime: String = "",
               tripleColumn: Boolean = false, modifier: Modifier = Modifier.fillMaxWidth()) {
    val personDC = runBlocking { Repository.personsDao.getByEmail(email) }
    val profile = personDC?.profile?.deserialize()
    val name = personDC?.nickname ?: profile?.displayName ?: email
    ContactRow(name, hasLast, message, messageTime, tripleColumn) {
        ProfileImage(personDC, email)
    }
}
@Composable
fun ContactRow(personDC: PersonDC, hasLast: Boolean = false, message: String = "", messageTime: String = "",
               tripleColumn: Boolean = false) {
    val profile = personDC.profile?.deserialize()
    val name = personDC.nickname ?: profile?.displayName ?: ""
    ContactRow(name, hasLast, message, messageTime, tripleColumn) {
        ProfileImage(personDC, personDC.personEmail)
    }
}

@Composable
fun ContactRow(name: String, hasLast: Boolean, message: String,
                       messageTime: String, tripleColumn: Boolean,
                       pfp: @Composable (ColumnScope.() -> Unit)) {
    Row(modifier = Modifier.fillMaxWidth().height(64.dp)) {
        Column(
            modifier = Modifier.width(64.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            pfp()
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(5.dp).weight(2.0f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(name, color = MaterialTheme.colorScheme.onBackground)
            if (hasLast) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    maxLines = if (tripleColumn) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(Modifier.padding(5.dp), verticalAlignment = Alignment.Top) {
            Text(messageTime, color = MaterialTheme.colorScheme.onSurface, autoSize = TextAutoSize.StepBased(8.sp, 12.sp))
        }
    }
}