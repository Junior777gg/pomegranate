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
import org.unstabledev.pomegranate.P2PUtils.Observer
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessagesDao
import org.unstabledev.pomegranate.database.serialize
import org.unstabledev.pomegranate.database.sha256

class MainScreenController(val chatDao: ChatDao, messagesDao: MessagesDao) : ViewModel() {
    private val _chats: MutableStateFlow<List<ChatDC>> = MutableStateFlow(emptyList())
    val chats: StateFlow<List<ChatDC>> = _chats

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _chats.value = chatDao.getAllChats()
        }

        viewModelScope.launch(Dispatchers.IO) {
            launch {
                LoggerImpl().init()
                while (true) {
                    val opponent = BaseP2P.receiveConnections()
                    val profile = try {
                        Gravatar.getProfile(opponent.first.sha256())
                    } catch (e: Exception) {
                        null
                    }
                    val chat = ChatDC(opponent.first, profile?.serialize())
                    val observer = Observer(opponent.second, chat, messagesDao)
                    chatDao.upsertChat(chat)
                    _chats.value = chatDao.getAllChats()
                    Repository.availableChats[chat] = observer
                }
            }

            launch {
                while (true) {
                    val last = Repository.lastContact
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