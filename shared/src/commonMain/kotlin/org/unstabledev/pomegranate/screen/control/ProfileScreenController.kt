package org.unstabledev.pomegranate.screen.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.api.ext.Gravatar
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.sha256
import org.unstabledev.pomegranate.screen.Profile

class ProfileScreenController(val chatKey: Long?) : ViewModel() {
    private val chatDao = Repository.chatDao
    private val chat = MutableStateFlow<ChatDC?>(null)
    val profile = MutableStateFlow(Profile())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            chat.value = if (chatKey!=null) chatDao.getByKey(chatKey).first() else null
        }
    }
    suspend fun getProfile(email: String): Profile? = withContext(Dispatchers.IO) {
        Gravatar.getProfile(email.sha256()).also {
            profile.value = it?:Profile()
        }
    }

    fun getChat() = chat

    fun invite(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val curChat = chat.value?:return@launch
            if (curChat.personsEmails.contains(email)) return@launch
            val observer = Repository.availablePersons[email]?.first()
            val currentChat = curChat.apply {
                personsEmails.add(email)
            }
            chatDao.upsert(currentChat)
            val message = Repository.createMessage(
                currentChat,
                type = MessageDC.INVITE,
                message = email,
                supData = Json.encodeToString(currentChat.personsEmails).encodeToByteArray()
            )
            if (observer != null) observer.sendMessage(message)
            else Repository.startMessaging(email, mutableListOf(message))
        }
    }
}