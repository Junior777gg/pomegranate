package org.unstabledev.pomegranate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import kotlinx.serialization.json.Json
import org.unstabledev.pomegranate.database.sha256
import org.unstabledev.pomegranate.screen.Profile

class ProfileScreenController : ViewModel() {
    val profile = MutableStateFlow(Profile())
    fun getProfile(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            profile.emit(Gravatar.getProfile(email.sha256()))
        }
    }
}