package org.unstabledev.pomegranate.screen.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.platform.PlatformEvents

class HomeScreenController : ViewModel() {
    val chatDao = Repository.chatDao
    val messagesDao = Repository.messagesDao
    val personDao = Repository.personsDao
    private val _chats: MutableStateFlow<List<ChatDC>> = MutableStateFlow(emptyList())
    val chats: StateFlow<List<ChatDC>> = _chats

    init {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.getAll().collect { _chats.value = it }
        }
    }

    fun deleteChat(chat: ChatDC) {
        viewModelScope.launch(Dispatchers.Default) {
            messagesDao.deleteAll(chat.chatName, chat.chatCreator, chat.chatType)
            chatDao.delete(chat)
        }
    }

    fun renameChat(chat: ChatDC, name: String?) {
        if (name.isNullOrBlank()) return
        viewModelScope.launch(Dispatchers.Default) {
            val newChat = chat.copy(chatName = name)
            if (chatDao.exists(newChat.chatName, chat.chatCreator, chat.chatType)) return@launch
            if (chat.chatType == ChatDC.Companion.ChatTypes.CHAT) {
                val newPerson = personDao.getByEmail(chat.personsEmails[0])?.copy(nickname = name)
                if (newPerson != null) {
                    personDao.upsert(newPerson)
                }
            }
            chatDao.upsert(newChat)
        }
    }

    fun deleteMessages(chat: ChatDC) {
        viewModelScope.launch(Dispatchers.Default) {
            messagesDao.deleteAll(chat.chatName, chat.chatCreator, chat.chatType)
        }
    }

    fun deleteEmptyChats() {
        viewModelScope.launch(Dispatchers.Default) {
            for(chat in chatDao.getAll().first()) {
                deleteChatIfEmpty(chat)
            }
        }
    }
    fun deleteChatIfEmpty(chat: ChatDC) {
        viewModelScope.launch(Dispatchers.Default) {
            if (chat.chatType != ChatDC.Companion.ChatTypes.GROUP) {
                val last = Repository.messagesDao.tryGetLast(chat.chatName, chat.chatCreator, chat.chatType).first()
                if (last == null) Repository.chatDao.delete(chat)
            }
        }
    }
}