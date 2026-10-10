package org.unstabledev.pomegranate.screen.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.api.ext.Gravatar
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.sha256
import org.unstabledev.pomegranate.screen.Profile

class ProfileScreenController(val chatKey: Long?) : ViewModel() {
    private val chatDao = Repository.chatDao
    private val chat = runBlocking {
        if (chatKey != null) chatDao.getByKey(chatKey)
        else null
    }
    val profile = MutableStateFlow(Profile())
    fun getProfile(email: String): Profile? {
        return runBlocking { Gravatar.getProfile(email.sha256()) }
    }

    fun getChat() = chat

    fun invite(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val observer = Repository.availablePersons[email]?.first()
            val message = Repository.createMessage(chat!!, type = MessageDC.INVITE)
            if (observer != null) {
                observer.sendMessage(message)
            }else{
                Repository.startMessaging(email, mutableListOf(message))
            }
        }
    }
}