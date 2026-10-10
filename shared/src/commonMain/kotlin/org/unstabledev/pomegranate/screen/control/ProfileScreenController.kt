package org.unstabledev.pomegranate.screen.control

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.api.ext.Gravatar
import org.unstabledev.pomegranate.database.sha256
import org.unstabledev.pomegranate.screen.Profile

class ProfileScreenController(val chatKey: Long?) : ViewModel() {
    private val chatDao = Repository.chatDao
    private val chat = runBlocking {
        if(chatKey!=null) chatDao.getByKey(chatKey)
        else null
    }
    val profile = MutableStateFlow(Profile())
    fun getProfile(email: String): Profile? {
        return runBlocking { Gravatar.getProfile(email.sha256()) }
    }
    fun getChat() = chat
}