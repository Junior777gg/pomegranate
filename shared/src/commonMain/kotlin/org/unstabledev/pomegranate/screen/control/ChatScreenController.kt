package org.unstabledev.pomegranate.screen.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.BaseP2P
import org.unstabledev.pomegranate.P2PUtils.Observer
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Repository.availableChats
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
    private var observer: Observer? = null
    val chatDC = MutableStateFlow(initialChat)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                availableChats.getOrPut(initialChat, { MutableSharedFlow(1) }).collect {
                    observer = it
                }
            }
            launch {
                while (true) {
                    _messages.value = messagesDao.getAllByEmail(initialChat.partnerEmail)
                    delay(1000)
                }
            }
        }
    }

    fun startMessaging(message: String? = null, files: List<Pair<ByteArray, String>>? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val messages = mutableListOf<MessageDC>()
            if (message != null) {
                val messageDC = Repository.createMessage(initialChat, message)
                messagesDao.insertMessage(messageDC)
                messages.add(messageDC)
            }
            if (files != null) {
                files.forEach { file ->
                    val messageDC = Repository.createMessage(initialChat, file = file)
                    messagesDao.insertMessage(messageDC)
                    messages.add(messageDC)
                }
            }
            try {
                val manager = BaseP2P().createConnection(initialChat.partnerEmail)
                observer = Observer(
                    manager,
                    manager.channel!!,
                    initialChat,
                    messagesDao
                )
                availableChats.getOrPut(initialChat, { MutableSharedFlow(1) }).emit(observer)
                messages.forEach {
                    observer?.send(it)
                }

            } catch (_: TimeoutCancellationException) {
                if (Repository.waitedConnection[chatDC.value] == null) {
                    Repository.waitedConnection[chatDC.value] = messages
                } else {
                    Repository.waitedConnection[chatDC.value]!!.addAll(messages)
                }
            }
        }
    }

    fun send(message: String? = null, files: List<Pair<ByteArray, String>>? = null) {
        if (observer == null) {
            startMessaging(message, files)
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                if (message != null) {
                    val messageDC = Repository.createMessage(initialChat, message)
                    messagesDao.insertMessage(messageDC)
                    observer!!.send(messageDC)

                }
                if (files != null) {
                    files.forEach { file ->
                        val messageDC = Repository.createMessage(initialChat, file = file)
                        messagesDao.insertMessage(messageDC)
                        observer!!.send(messageDC)
                    }
                }
            }
        }
    }
}