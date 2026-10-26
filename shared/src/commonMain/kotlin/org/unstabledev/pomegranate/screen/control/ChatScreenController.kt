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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.unstabledev.pomegranate.BaseP2P
import org.unstabledev.pomegranate.platform.KMPFile
import org.unstabledev.pomegranate.P2PUtils.Observer
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.deserialize
import org.unstabledev.pomegranate.screen.Profile
import kotlin.time.Clock


class ChatScreenController(val chatKey: Long, val onChatDelete: () -> Unit) : ViewModel() {
    private val pageSizeStep = 40
    private val _pageSize = MutableStateFlow(pageSizeStep)
    private val messagesDao = Repository.messagesDao
    private val chatDao = Repository.chatDao
    private val personDao = Repository.personsDao
    private val isOnline = MutableStateFlow(false)
    private val chatDC = MutableStateFlow<ChatDC?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            chatDC.value = chatDao.getByKey(chatKey).first()
        }
    }

    fun getChat() = chatDC.value!!

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<MessageDC>> = _pageSize
        .flatMapLatest { limit ->
            messagesDao.getPaged(getChat().chatName, getChat().chatCreator, getChat().chatType, limit)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    fun loadMore() {
        _pageSize.value += pageSizeStep
    }

    fun send(message: String? = null, files: List<KMPFile>, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val messagesList = mutableListOf<MessageDC>()
            val currentChat = getChat()

            val totalItems = (if (message!=null) 1 else 0) + files.size
            val bulkGroupId = if (totalItems>1) Clock.System.now().toEpochMilliseconds() else 0L
            if (message != null && type == MessageDC.TEXT) {
                val messageDC =
                    Repository.createMessage(currentChat, message = message, type = MessageDC.TEXT, bulkId = bulkGroupId)
                messagesDao.insert(messageDC)
                messagesList.add(messageDC)
            }
            if (message == null && type == MessageDC.INVITE) {
                val messageDC =
                    Repository.createMessage(currentChat, type = MessageDC.INVITE,
                        supData = Json.encodeToString(currentChat.personsEmails).encodeToByteArray(), bulkId = bulkGroupId)
                messagesDao.insert(messageDC)
                messagesList.add(messageDC)
            }
            if (message == null && type == MessageDC.BEGIN_CALL) {
                val messageDC = Repository.createMessage(currentChat, type = MessageDC.BEGIN_CALL, bulkId = bulkGroupId)
                messagesDao.insert(messageDC)
                messagesList.add(messageDC)
            }
            if (files.isNotEmpty()) {
                files.forEach { file ->
                    val messageDC =
                        Repository.createMessage(currentChat, file = file, type = MessageDC.FILE, bulkId = bulkGroupId)
                    messagesDao.insert(messageDC)
                    messagesList.add(messageDC)
                }
            }
            when (currentChat.chatType) {
                ChatDC.Companion.ChatTypes.CHAT -> {
                    val observer = Repository.availablePersons[currentChat.personsEmails[0]]?.first()
                    if (observer == null) {
                        Repository.startMessaging(currentChat.personsEmails[0], messagesList)
                    } else {
                        messagesList.forEach { message ->
                            observer.sendMessage(message)
                        }
                    }
                }

                ChatDC.Companion.ChatTypes.GROUP -> {
                    val clients = currentChat.personsEmails.toMutableList()
                    clients.remove(Repository.myEmail)
                    println(clients)
                    if (clients.isEmpty()) {return@launch}
                    if (clients.size == 1) {
                        val observer = Repository.availablePersons[clients.first()]?.first()
                        if (observer != null) {
                            messagesList.forEach { message ->
                                val currentMessage = message.apply {
                                    supData = Json.encodeToString(clients).encodeToByteArray()
                                }
                                observer.sendMessage(currentMessage)
                            }
                        } else {
                            messagesList.forEach { message ->
                                message.apply {
                                    supData = Json.encodeToString(clients).encodeToByteArray()
                                }
                            }
                            Repository.startMessaging(clients.first(), messagesList)
                        }
                        return@launch
                    }
                    val pair = if (clients.size % 2 == 0) {
                        clients.chunked(clients.size / 2)
                    } else {
                        clients.chunked((clients.size - 1) / 2)
                    }
                    for (each in pair) {
                        val observer = Repository.availablePersons[each.first()]?.first()
                        if (observer != null) {
                            messagesList.forEach { message ->
                                val currentMessage = message.apply {
                                    supData = Json.encodeToString(each).encodeToByteArray()
                                }
                                observer.sendMessage(currentMessage)
                            }
                        } else {
                            messagesList.forEach { message ->
                                message.apply {
                                    supData = Json.encodeToString(each).encodeToByteArray()
                                }
                            }
                            Repository.startMessaging(each.first(), messagesList)
                        }
                    }
                }
            }
        }
    }

    fun deleteChat() {
        viewModelScope.launch(Dispatchers.Default) {
            chatDao.delete(getChat())
            messagesDao.deleteAll(getChat().chatName, getChat().chatCreator, getChat().chatType)
            onChatDelete()
        }
    }

    fun deleteMessages() {
        viewModelScope.launch(Dispatchers.Default) {
            messagesDao.deleteAll(getChat().chatName, getChat().chatCreator, getChat().chatType)
        }
    }

    fun getName(): String {
        return getProfile(getChat().personsEmails[0])?.displayName ?:
        //personDao.tryGetPersonByEmailFlow(chatDC.personsEmails[0]).first()?.personEmail?:
        getChat().chatName
    }

    fun renameChat(name: String?) {
        viewModelScope.launch(Dispatchers.Default) {
            if (name.isNullOrBlank()) return@launch
            val newChat = getChat().copy(chatName = name)
            if (chatDao.exists(newChat.chatName, getChat().chatCreator, getChat().chatType)) return@launch
            if (getChat().chatType == ChatDC.Companion.ChatTypes.CHAT) {
                val newPerson = personDao.getByEmail(getChat().personsEmails[0])?.copy(nickname = name)
                if (newPerson != null) {
                    personDao.upsert(newPerson)
                }
            }
            chatDao.upsert(newChat)
        }
    }

    fun getProfile(email: String): Profile? {
        return runBlocking(Dispatchers.Default) { personDao.getByEmail(email)?.profile?.deserialize() }
    }

    fun isOnline(): MutableStateFlow<Boolean> {
        return isOnline
    }

}