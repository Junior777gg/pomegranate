package org.unstabledev.pomegranate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.P2PUtils.LoggerImpl
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessagesDao

class MainScreenController(val chatDao: ChatDao, messagesDao: MessagesDao) : ViewModel() {
    private val _chats: MutableStateFlow<List<ChatDC>> = MutableStateFlow(emptyList())
    val chats: StateFlow<List<ChatDC>> = _chats

    init {
        viewModelScope.launch(Dispatchers.IO) {
            LoggerImpl().init()
            _chats.value = chatDao.getAllChats()
            launch {
                _chats.value = chatDao.getAllChats()
                delay(1000)
            }
            launch {
                Repository.lastContact.collect { last ->
                    if (last != null) {
                        val currentChats = chatDao.getAllChats()
                        if (!currentChats.contains(last.first)) {
                            chatDao.upsertChat(last.first)
                        }
                        if (Repository.availableChats[last.first] == null && last.second != null) {
                            Repository.availableChats[last.first] = last.second!!
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