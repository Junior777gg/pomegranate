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
import org.unstabledev.pomegranate.P2PUtils.P2PChannelImpl
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.serialize
import org.unstabledev.pomegranate.database.sha256
import org.unstabledev.pomegranate.screen.Profile
import kotlin.random.Random


class MainScreenController(val chatDao: ChatDao) : ViewModel() {
    private val _chats: MutableStateFlow<MutableList<ChatDC>> = MutableStateFlow(mutableListOf())
    val chats: StateFlow<MutableList<ChatDC>> = _chats
    init {
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
                    chatDao.upsertChat(chat)
                    _chats.value = chatDao.getAllChats().toMutableList()
                    Repository.availableChats[chat] = Observer(opponent.second)
                }
            }
            launch {
                var list = chatDao.getAllChats()
                while (true) {
                    val last = Repository.lastContact
                    if (last != null) {
                        if (!list.contains(last.first)) {
                            chatDao.upsertChat(last.first)
                            list = chatDao.getAllChats()
                        }
                        if (Repository.availableChats[last.first] == null && last.second != null) {
                            Repository.availableChats[last.first] = last.second!!
                        }
                    }
                    delay(1000)
                }
            }
        }
    }
    suspend fun update() {
        _chats.value = chatDao.getAllChats().toMutableList()
    }
}
