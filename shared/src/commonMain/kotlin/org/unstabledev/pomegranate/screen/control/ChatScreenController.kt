package org.unstabledev.pomegranate.screen.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.BaseP2P
import org.unstabledev.pomegranate.P2PUtils.Observer
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.MessagesDao
import kotlin.time.Clock.System.now

class ChatScreenController(
    val messagesDao: MessagesDao,
    val initialChat: ChatDC,
) : ViewModel() {
    private val _messages: MutableStateFlow<List<MessageDC>> = MutableStateFlow(listOf())
    val messages: StateFlow<List<MessageDC>> = _messages
    private var observer: Observer? = Repository.availableChats[initialChat]
    val chatDC = MutableStateFlow(initialChat)

    init {
        viewModelScope.launch(Dispatchers.IO) {
                while (true) {
                    _messages.value = messagesDao.getAllByEmail(initialChat.partnerEmail)
                    delay(1000)
            }
        }
    }

    fun startMessaging(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (observer == null) {
                try {
                    val time = now().toString().split("T")[1].split(":")
                    val messageDC = MessageDC(
                        email = chatDC.value.partnerEmail,
                        data = message.encodeToByteArray(),
                        type = MessageDC.TEXT,
                        time = "${time[0].toInt() + 3}:${time[1]}",
                        isMine = true,
                    )
                    messagesDao.insertMessage(messageDC)
                    val manager = BaseP2P().createConnection(initialChat.partnerEmail)
                    observer = Observer(
                        manager,
                        manager.channel!!,
                        initialChat,
                        messagesDao
                    )
                    Repository.availableChats[initialChat] = observer!!
                    observer!!.send(message)

                } catch (e: TimeoutCancellationException) {
                    if (Repository.waitedConnection[chatDC.value] == null) {
                        Repository.waitedConnection[chatDC.value] = mutableListOf(message)
                    } else {
                        Repository.waitedConnection[chatDC.value]!!.add(message)
                    }
                }
            }
        }
    }

    fun send(message: String) {
        if (observer == null) {
            startMessaging(message)
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                val time = now().toString().split("T")[1].split(":")
                val messageDC = MessageDC(
                    email = chatDC.value.partnerEmail,
                    data = message.encodeToByteArray(),
                    type = MessageDC.TEXT,
                    time = "${time[0].toInt() + 3}:${time[1]}",
                    isMine = true,
                )
                messagesDao.insertMessage(messageDC)
                observer?.send(message)
            }
        }
    }
}