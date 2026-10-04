package org.unstabledev.pomegranate.screen.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.api.Gravatar
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.PersonDC
import org.unstabledev.pomegranate.database.serialize
import org.unstabledev.pomegranate.database.sha256

class ContactScreenController : ViewModel() {
    fun createChat(email: String): ChatDC {
        val profile = try {
            runBlocking { Gravatar.getProfile(email.sha256()) }
        } catch (_: Exception) {
            null
        }
        val chat = ChatDC(
            chatName = email,
            personsEmails = listOf(email),
            chatType = ChatDC.Companion.ChatTypes.CHAT,
            chatCreator = email
        )
        val person = PersonDC(
            personEmail = email,
            profile = profile?.serialize()
        )
        viewModelScope.launch {
            Repository.personsDao.upsert(person)
        }
        val savedChat = runBlocking { Repository.chatDao.upsertAndGet(chat) }
        return savedChat
    }

    fun createGroups(name: String): ChatDC {
        val chat = ChatDC(
            chatName = name,
            personsEmails = listOf(Repository.myEmail),
            chatType = ChatDC.Companion.ChatTypes.GROUP,
            chatCreator = Repository.myEmail
        )
        val savedChat = runBlocking { Repository.chatDao.upsertAndGet(chat) }
        return savedChat
    }
}