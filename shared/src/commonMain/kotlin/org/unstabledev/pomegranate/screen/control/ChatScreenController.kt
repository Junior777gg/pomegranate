package org.unstabledev.pomegranate.screen.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.unstabledev.pomegranate.BaseP2P
import org.unstabledev.pomegranate.KMPFile
import org.unstabledev.pomegranate.P2PUtils.Observer
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.deserialize
import org.unstabledev.pomegranate.screen.Profile


class ChatScreenController : ViewModel() {
    private val pageSizeStep = 40
    private val _pageSize = MutableStateFlow(pageSizeStep)
    private val chatDC = Repository.lastChat.value!!
    private val messagesDao = Repository.messagesDao
    private val chatDao = Repository.chatDao
    private val personDao = Repository.personsDao
    private val isOnline = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<MessageDC>> = _pageSize
        .flatMapLatest { limit ->
            messagesDao.getPaged(chatDC.chatName, chatDC.chatCreator, chatDC.chatType, limit)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    private var observers: MutableMap<String, Observer?> = mutableMapOf()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                while (true) {
                    chatDC.personsEmails.forEach { email ->
                        Repository.availablePersons.getOrPut(email) { MutableSharedFlow(1) }.collect {
                            observers[email] = it
                        }
                    }
                }
            }
        }
    }

    fun loadMore() {
        _pageSize.value += pageSizeStep
    }

    fun startMessaging(message: String? = null, files: List<KMPFile>? = null, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentChat = chatDC
            val messagesList = mutableListOf<MessageDC>()
            if (message != null && type == MessageDC.TEXT) {
                val messageDC = Repository.createMessage(currentChat, message = message, type = MessageDC.TEXT)
                messagesDao.insertMessage(messageDC)
                messagesList.add(messageDC)
            }
            if (message == null && type == MessageDC.BEGIN_CALL) {
                val messageDC = Repository.createMessage(currentChat, type = MessageDC.BEGIN_CALL)
                messagesDao.insertMessage(messageDC)
                messagesList.add(messageDC)
            }
            if (files != null) {
                files.forEach { file ->
                    val messageDC = Repository.createMessage(currentChat, file = file, type = MessageDC.FILE)
                    messagesDao.insertMessage(messageDC)
                    messagesList.add(messageDC)
                }
            }
            when (chatDC.chatType) {
                ChatDC.Companion.ChatTypes.CHAT -> {
                    val email = chatDC.personsEmails[0]
                    val manager = BaseP2P().createConnection(email)
                    try {
                        observers[email] = Observer(
                            manager,
                            manager.channel!!,
                            email,
                            messagesDao
                        )
                        Repository.availablePersons.getOrPut(email) { MutableSharedFlow(1) }.emit(observers[email])
                        messagesList.forEach {
                            observers[email]?.sendMessage(it)
                        }
                    } catch (_: TimeoutCancellationException) {
                        if (Repository.waitedConnection[email] == null) {
                            Repository.waitedConnection[email] = messagesList
                        } else {
                            Repository.waitedConnection[email]!!.addAll(messagesList)
                        }
                    }
                }

                ChatDC.Companion.ChatTypes.GROUP -> {

                }
            }
        }
    }


    fun send(message: String? = null, files: List<KMPFile>? = null, type: String) {
        when (chatDC.chatType) {
            ChatDC.Companion.ChatTypes.CHAT -> {
                val observer = observers[chatDC.personsEmails[0]]
                if (observer == null) {
                    startMessaging(message, files, type)
                } else {
                    viewModelScope.launch(Dispatchers.IO) {
                        val currentChat = chatDC
                        if (message != null && type == MessageDC.TEXT) {
                            val messageDC =
                                Repository.createMessage(currentChat, message = message, type = MessageDC.TEXT)
                            messagesDao.insertMessage(messageDC)
                            observer.sendMessage(messageDC)
                        }
                        if (message == null && type == MessageDC.BEGIN_CALL) {
                            val messageDC = Repository.createMessage(currentChat, type = MessageDC.BEGIN_CALL)
                            messagesDao.insertMessage(messageDC)
                            observer.sendMessage(messageDC)
                        }
                        if (files != null) {
                            files.forEach { file ->
                                val messageDC =
                                    Repository.createMessage(currentChat, file = file, type = MessageDC.FILE)
                                messagesDao.insertMessage(messageDC)
                                observer.sendMessage(messageDC)
                            }
                        }
                    }
                }
            }

            ChatDC.Companion.ChatTypes.GROUP -> {}
        }
    }

    fun deleteChat() {
        viewModelScope.launch(Dispatchers.Default) {
            chatDao.deleteChat(chatDC)
            messagesDao.deleteAll(chatDC.chatName, chatDC.chatCreator, chatDC.chatType)
        }
    }

    fun deleteMessages() {
        viewModelScope.launch(Dispatchers.Default) {
            messagesDao.deleteAll(chatDC.chatName, chatDC.chatCreator, chatDC.chatType)
        }
    }

    fun clearLastChat() {
        Repository.setLastChat(null)
    }

    fun getName(): String {
        return chatDC.chatName
    }

    fun renameChat(name: String?) {
        viewModelScope.launch(Dispatchers.Default) {
            if (name.isNullOrBlank()) return@launch
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

    fun getChat(): ChatDC {
        return chatDC
    }

    fun getProfile(email: String): Profile? {
        return runBlocking(Dispatchers.Default) { personDao.getPersonByEmail(email)?.profile?.deserialize() }
    }

    fun isOnline(): MutableStateFlow<Boolean> {
        return isOnline
    }

}