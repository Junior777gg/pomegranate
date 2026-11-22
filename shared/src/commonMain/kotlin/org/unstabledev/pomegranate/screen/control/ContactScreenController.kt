package org.unstabledev.pomegranate.screen.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.api.ext.Gravatar
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.PersonDC
import org.unstabledev.pomegranate.database.serialize
import org.unstabledev.pomegranate.database.sha256

class ContactScreenController : ViewModel() {
    suspend fun createChat(email: String): ChatDC {
        val profile = try {
            Gravatar.getProfile(email.sha256())
        } catch (_: Exception) {
            null
        }
        val person = PersonDC(
            personEmail = email,
            profile = profile?.serialize()
        )
        val chat = ChatDC(
            chatName = profile?.displayName ?: email,
            personsEmails = mutableListOf(email),
            chatType = ChatDC.Companion.ChatTypes.CHAT,
            chatCreator = email,
            pinnedMessages = mutableListOf()
        )
        viewModelScope.launch {
            Repository.personsDao.upsert(person)
        }
        val savedChat = Repository.chatDao.upsertAndGet(chat).first()
        return savedChat
    }

    suspend fun createGroup(name: String): ChatDC {
        val chat = ChatDC(
            chatName = name,
            personsEmails = mutableListOf(Repository.myEmail),
            chatType = ChatDC.Companion.ChatTypes.GROUP,
            chatCreator = Repository.myEmail,
            pinnedMessages = mutableListOf()
        )
        val savedChat = Repository.chatDao.upsertAndGet(chat).first()
        return savedChat
    }
}