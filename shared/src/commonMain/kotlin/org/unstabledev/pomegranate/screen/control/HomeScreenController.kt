package org.unstabledev.pomegranate.screen.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.ChatDao

class HomeScreenController(val chatDao: ChatDao) : ViewModel() {
    private val _chats: MutableStateFlow<List<ChatDC>> = MutableStateFlow(emptyList())
    val chats: StateFlow<List<ChatDC>> = _chats

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _chats.value = chatDao.getAllChats()
            launch {
                while (true) {
                    _chats.value = chatDao.getAllChats()
                    delay(1000)
                }
            }
            launch {
                Repository.lastContact.collect { last ->
                    if (last != null) {
                        val currentChats = chatDao.getAllChats()
                        if (!currentChats.contains(last)) {
                            chatDao.upsertChat(last)
                        }
                        _chats.value = chatDao.getAllChats()
                    }
                    delay(1000)
                }
            }
        }
    }

    suspend fun update() {
        _chats.value = chatDao.getAllChats()
    }
}