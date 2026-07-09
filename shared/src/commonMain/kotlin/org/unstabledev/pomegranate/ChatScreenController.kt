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
import org.unstabledev.pomegranate.database.serialize
import org.unstabledev.pomegranate.screen.Profile
import kotlin.time.Clock.System.now

class ChatScreenController(val messagesDao: MessagesDao) : ViewModel() {
    private val _messages: MutableStateFlow<List<MessageDC>> = MutableStateFlow(listOf())
    val messages: StateFlow<List<MessageDC>> = _messages
    private lateinit var observer: Observer
    val chatDC = MutableStateFlow(ChatDC("", Profile().serialize()))
    private val receivedMessages = mutableListOf<String>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                chatDC.emit(Repository.lastContact!!.first)
                if (Repository.lastContact?.second == null) {
                    observer = Observer(
                        BaseP2P().createConnection(Repository.lastContact!!.first.partnerEmail),
                        chatDC.value,
                        messagesDao
                    )
                    Repository.availableChats[chatDC.value] = observer
                } else {
                    observer = Repository.lastContact!!.second!!
                }
                receive()
            } catch (_: Exception) { }
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
            observer.send(message)
            val time = now().toString().split("T")[1].split(":")
            val message = MessageDC(
                email = chatDC.value.partnerEmail,
                data = message.encodeToByteArray(),
                type = MessageDC.TEXT,
                time = "${time[0].toInt() + 3}:${time[1]}",
                isMine = true,
            )
            saveMessages(message)
            update(message)
        }
    }

    suspend fun receive() {
        observer.messages.collect {
            update(it)
        }
    }
}