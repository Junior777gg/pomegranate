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
            chatDao.getAllChatsFlow().collect { _chats.value = it }
            launch {
                while (true) {
                    chatDao.getAllChatsFlow().collect { _chats.value = it }
                    delay(1000)
                }
            }
            launch {
                Repository.lastContact.collect { last ->
                    if (last != null) {
                        val currentChats = _chats.value
                        if (!currentChats.contains(last)) {
                            chatDao.upsertChat(last)
                        }
                        chatDao.getAllChatsFlow().collect { _chats.value = it }
                    }
                    delay(1000)
                }
            }
        }
    }
}