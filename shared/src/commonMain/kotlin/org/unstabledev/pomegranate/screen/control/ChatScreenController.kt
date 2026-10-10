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


class ChatScreenController(val chatKey: Long, val onChatDelete: () -> Unit) : ViewModel() {
    private val pageSizeStep = 40
    private val _pageSize = MutableStateFlow(pageSizeStep)
    private val messagesDao = Repository.messagesDao
    private val chatDao = Repository.chatDao
    private val personDao = Repository.personsDao
    private val isOnline = MutableStateFlow(false)
    private val chatDC = runBlocking { chatDao.getByKey(chatKey) }

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

    fun loadMore() {
        _pageSize.value += pageSizeStep
    }

    fun send(message: String? = null, files: List<KMPFile>, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val messagesList = mutableListOf<MessageDC>()
            val currentChat = chatDC
            if (message != null && type == MessageDC.TEXT) {
                val messageDC =
                    Repository.createMessage(currentChat, message = message, type = MessageDC.TEXT)
                messagesDao.insert(messageDC)
                messagesList.add(messageDC)
            }
            if (message == null && type == MessageDC.INVITE) {
                val messageDC =
                    Repository.createMessage(currentChat, type = MessageDC.INVITE,
                        supData = Json.encodeToString(chatDC.personsEmails).encodeToByteArray())
                messagesDao.insert(messageDC)
                messagesList.add(messageDC)
            }
            if (message == null && type == MessageDC.BEGIN_CALL) {
                val messageDC = Repository.createMessage(currentChat, type = MessageDC.BEGIN_CALL)
                messagesDao.insert(messageDC)
                messagesList.add(messageDC)
            }
            if (files.isNotEmpty()) {
                files.forEach { file ->
                    val messageDC =
                        Repository.createMessage(currentChat, file = file, type = MessageDC.FILE)
                    messagesDao.insert(messageDC)
                    messagesList.add(messageDC)
                }
            }
            when (chatDC.chatType) {
                ChatDC.Companion.ChatTypes.CHAT -> {
                    val observer = Repository.availablePersons[chatDC.personsEmails[0]]?.first()
                    if (observer == null) {
                        Repository.startMessaging(chatDC.personsEmails[0], messagesList)
                    } else {
                        messagesList.forEach { message ->
                            observer.sendMessage(message)
                        }
                    }
                }

                ChatDC.Companion.ChatTypes.GROUP -> {
                    val clients = chatDC.personsEmails.toMutableList()
                    clients.remove(Repository.myEmail)
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
            chatDao.delete(chatDC)
            messagesDao.deleteAll(chatDC.chatName, chatDC.chatCreator, chatDC.chatType)
            onChatDelete()
        }
    }

    fun deleteMessages() {
        viewModelScope.launch(Dispatchers.Default) {
            messagesDao.deleteAll(chatDC.chatName, chatDC.chatCreator, chatDC.chatType)
        }
    }

    fun getName(): String {
        return getProfile(getChat().personsEmails[0])?.displayName ?:
        //personDao.tryGetPersonByEmailFlow(chatDC.personsEmails[0]).first()?.personEmail?:
        chatDC.chatName
    }

    fun renameChat(name: String?) {
        viewModelScope.launch(Dispatchers.Default) {
            if (name.isNullOrBlank()) return@launch
            val newChat = chatDC.copy(chatName = name)
            if (chatDao.exists(newChat.chatName, chatDC.chatCreator, chatDC.chatType)) return@launch
            if (chatDC.chatType == ChatDC.Companion.ChatTypes.CHAT) {
                val newPerson = personDao.getByEmail(chatDC.personsEmails[0])?.copy(nickname = name)
                if (newPerson != null) {
                    personDao.upsert(newPerson)
                }
            }
            chatDao.upsert(newChat)
        }
    }

    fun getChat(): ChatDC {
        return chatDC
    }

    fun getProfile(email: String): Profile? {
        return runBlocking(Dispatchers.Default) { personDao.getByEmail(email)?.profile?.deserialize() }
    }

    fun isOnline(): MutableStateFlow<Boolean> {
        return isOnline
    }

}