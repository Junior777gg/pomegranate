package org.unstabledev.pomegranate.screen.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
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
    private val PAGE_SIZE_STEP = 40
    private val _pageSize = MutableStateFlow(PAGE_SIZE_STEP)

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<MessageDC>> = _pageSize
        .flatMapLatest { limit ->
            messagesDao.getPagedByEmail(initialChat.partnerEmail, limit)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )
    private var observer: Observer? = null
    val chatDC = MutableStateFlow(initialChat)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            availableChats.getOrPut(initialChat, { MutableSharedFlow(1) }).collect {
                observer = it
            }
        }
    }

    fun loadMore() {
        _pageSize.value += PAGE_SIZE_STEP
    }

    fun startMessaging(message: String? = null, files: List<Pair<ByteArray, String>>? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val messagesList = mutableListOf<MessageDC>()
            if (message != null) {
                val messageDC = Repository.createMessage(initialChat, message)
                messagesDao.insertMessage(messageDC)
                messagesList.add(messageDC)
            }
            files?.forEach { file ->
                val messageDC = Repository.createMessage(initialChat, file = file)
                messagesDao.insertMessage(messageDC)
                messagesList.add(messageDC)
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
                messagesList.forEach {
                    observer?.sendMessage(it)
                }

            } catch (_: TimeoutCancellationException) {
                if (Repository.waitedConnection[chatDC.value] == null) {
                    Repository.waitedConnection[chatDC.value] = messagesList
                } else {
                    Repository.waitedConnection[chatDC.value]!!.addAll(messagesList)
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
                    observer!!.sendMessage(messageDC)
                }
                files?.forEach { file ->
                    val messageDC = Repository.createMessage(initialChat, file = file)
                    messagesDao.insertMessage(messageDC)
                    observer!!.sendMessage(messageDC)
                }
            }
        }
    }
}