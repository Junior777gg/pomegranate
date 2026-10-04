package org.unstabledev.pomegranate.components.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.runBlocking
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.components.ProfileImage
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.PersonDC
import org.unstabledev.pomegranate.database.deserialize

@Composable
fun ContactRow(chat: ChatDC, hasLast: Boolean = false, message: String = "", tripleColumn: Boolean = false) {
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
    Row(modifier = Modifier.fillMaxWidth().height(64.dp)) {
        Column(
            modifier = Modifier.width(64.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileImage(chat)
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(5.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                name,
                color = MaterialTheme.colorScheme.onBackground
            )
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
    }
}
@Composable
fun ContactRow(email: String, hasLast: Boolean = false, message: String = "", tripleColumn: Boolean = false) {
    val personDC = runBlocking { Repository.personsDao.getByEmail(email) }
    val profile = personDC?.profile?.deserialize()
    val name = personDC?.nickname ?: profile?.displayName ?: email
    Row(modifier = Modifier.fillMaxWidth().height(64.dp)) {
        Column(
            modifier = Modifier.width(64.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileImage(personDC, email)
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(5.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                name,
                color = MaterialTheme.colorScheme.onBackground
            )
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
    }
}
@Composable
fun ContactRow(personDC: PersonDC, hasLast: Boolean = false, message: String = "", tripleColumn: Boolean = false) {
    val profile = personDC.profile?.deserialize()
    val name = personDC.nickname ?: profile?.displayName ?: ""
    Row(modifier = Modifier.fillMaxWidth().height(64.dp)) {
        Column(
            modifier = Modifier.width(64.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileImage(personDC, personDC.personEmail)
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(5.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                name,
                color = MaterialTheme.colorScheme.onBackground
            )
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
    }
}