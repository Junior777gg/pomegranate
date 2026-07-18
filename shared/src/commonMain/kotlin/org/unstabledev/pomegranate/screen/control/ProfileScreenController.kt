package org.unstabledev.pomegranate.screen.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.unstabledev.pomegranate.api.Gravatar
import org.unstabledev.pomegranate.database.sha256
import org.unstabledev.pomegranate.screen.Profile

class ProfileScreenController : ViewModel() {
    val profile = MutableStateFlow(Profile())
    suspend fun getProfile(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            val p = Gravatar.getProfile(email.sha256())
            if (p != null) {
                profile.emit(p)
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