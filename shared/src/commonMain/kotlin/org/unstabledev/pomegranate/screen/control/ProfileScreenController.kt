package org.unstabledev.pomegranate.screen.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Util

import org.unstabledev.pomegranate.api.Gravatar
import org.unstabledev.pomegranate.database.serialize
import org.unstabledev.pomegranate.database.sha256
import org.unstabledev.pomegranate.screen.Profile

class ProfileScreenController : ViewModel() {
    val profile = MutableStateFlow(Profile())
    suspend fun getProfile(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            if(!Util.isValidEmail(email)) false
            val p=Gravatar.getProfile(email.sha256())
            if (p != null) {
                profile.emit(p)
                val chat=Repository.chatDao.getChatByEmailFlow(email).first()
                if (chat.profile==null || chat.profile!=profile.value.serialize()) {
                    chat.profile=profile.value.serialize()
                    Repository.chatDao.upsertChat(chat)
                }
                true
            } else false
        }
    }

    fun getProfileAsync(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(getProfile(email))
        }
    }
}