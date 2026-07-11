package org.unstabledev.pomegranate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.P2PUtils.Observer
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.MessagesDao
import kotlin.time.Clock.System.now

class ChatScreenController(
    val messagesDao: MessagesDao,
    initialChat: ChatDC,
    initialObserver: Observer?
) : ViewModel() {
    private val _messages: MutableStateFlow<List<MessageDC>> = MutableStateFlow(listOf())
    val messages: StateFlow<List<MessageDC>> = _messages
    private var observer: Observer? = initialObserver
    val chatDC = MutableStateFlow(initialChat)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _messages.value = messagesDao.getAllByEmail(initialChat.partnerEmail)

            if (observer == null) {
                try {
                    observer = Observer(
                        BaseP2P().createConnection(initialChat.partnerEmail),
                        initialChat,
                        messagesDao
                    )
                    Repository.availableChats[initialChat] = observer!!
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            receive()
        }
    }

    fun update() {
        viewModelScope.launch(Dispatchers.IO) {
            _messages.value = messagesDao.getAllByEmail(chatDC.value.partnerEmail)
        }
    }

    private suspend fun saveMessages(message: MessageDC) {
        try {
            messagesDao.insertMessage(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun update(message: MessageDC) {
        _messages.update {
            it + message
        }
    }

    fun send(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            observer?.send(message)
            val time = now().toString().split("T")[1].split(":")
            val messageDC = MessageDC(
                email = chatDC.value.partnerEmail,
                data = message.encodeToByteArray(),
                type = MessageDC.TEXT,
                time = "${time[0].toInt() + 3}:${time[1]}",
                isMine = true,
            )
            saveMessages(messageDC)
            update(messageDC)
        }
    }

    private suspend fun receive() {
        observer?.messages?.collect {
            update(it)
        }
    }
}