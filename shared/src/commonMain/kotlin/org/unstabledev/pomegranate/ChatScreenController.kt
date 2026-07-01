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
import kotlin.time.Clock.System.now

class ChatScreenController : ViewModel() {
    private val _messages: MutableStateFlow<List<MessageDC>> = MutableStateFlow(listOf())
    val messages: StateFlow<List<MessageDC>> = _messages
    private lateinit var observer: Observer
    private lateinit var chatDC: ChatDC

    init {
        viewModelScope.launch(Dispatchers.IO) {
            chatDC = Repository.lastContact!!.first
            if (Repository.lastContact?.second == null) {
                observer = Observer(BaseP2P().createConnection(Repository.lastContact!!.first.partnerEmail))
                Repository.availableChats[chatDC] = observer
            } else {
                observer = Repository.lastContact!!.second!!
            }
            receive()
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
                email = Repository.myEmail,
                data = message.encodeToByteArray(),
                type = MessageDC.TEXT,
                "${time[0].toInt() + 3}:${time[1]}",
                isMine = true,
            )
            update(message)
        }
    }

    suspend fun receive() {
        observer.messages.collect {
            val time = now().toString().split("T")[1].split(":")
            val message = MessageDC(
                email = chatDC.partnerEmail,
                data = it.encodeToByteArray(),
                type = MessageDC.TEXT,
                "${time[0].toInt() + 3}:${time[1]}",
                isMine = false,
            )
            update(message)
        }
    }
}