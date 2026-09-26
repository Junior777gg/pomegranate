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

class HomeScreenController : ViewModel() {
    val chatDao = Repository.chatDao
    val messagesDao = Repository.messagesDao
    val personDao = Repository.personsDao
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
                Repository.lastChat.collect { last ->
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

    fun setLastChat(chat: ChatDC?) {
        Repository.setLastChat(chat)
    }

    fun deleteChat() {
        viewModelScope.launch(Dispatchers.Default) {
            val chat = Repository.lastChat.value
            if (chat != null) {
                messagesDao.deleteAll(chat.chatName, chat.chatCreator, chat.chatType)
                chatDao.deleteChat(chat)
            }
        }
    }

    fun renameChat(name: String?) {
        viewModelScope.launch(Dispatchers.Default) {
            if (name.isNullOrBlank()) return@launch
            val chatDC = Repository.lastChat.value ?: return@launch
            val newChat = chatDC.copy(chatName = name)
            if (chatDao.isThisChatExists(newChat.chatName, chatDC.chatCreator, chatDC.chatType)) return@launch
            if (chatDC.chatType == ChatDC.Companion.ChatTypes.CHAT) {
                val newPerson = personDao.getPersonByEmail(chatDC.personsEmails[0])?.copy(nickname = name)
                if (newPerson != null) {
                    personDao.upsertPerson(newPerson)
                }
            }
            chatDao.upsertChat(newChat)
            Repository.setLastChat(newChat)
        }
    }

    fun deleteMessages() {
        viewModelScope.launch(Dispatchers.Default) {
            val chat = Repository.lastChat.value
            if (chat != null) {
                messagesDao.deleteAll(chat.chatName, chat.chatCreator, chat.chatType)
            }
        }
    }
}