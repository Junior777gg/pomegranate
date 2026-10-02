package org.unstabledev.pomegranate.screen.control

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.unstabledev.pomegranate.api.Gravatar
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.sha256
import org.unstabledev.pomegranate.screen.Profile

class ProfileScreenController(chat: ChatDC) : ViewModel() {
    val profile = MutableStateFlow(Profile())
    fun getProfile(email: String): Profile? {
        return runBlocking { Gravatar.getProfile(email.sha256()) }
    }
}