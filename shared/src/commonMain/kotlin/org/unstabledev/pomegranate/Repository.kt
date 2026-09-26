package org.unstabledev.pomegranate

import androidx.compose.runtime.mutableStateOf
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.P2PUtils.Observer
import org.unstabledev.pomegranate.P2PUtils.P2PManagerImpl
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.MessagesDao
import org.unstabledev.pomegranate.database.PersonDC
import org.unstabledev.pomegranate.database.PersonDao
import kotlin.getValue
import kotlin.time.Clock.System.now
import kotlin.time.Duration.Companion.milliseconds

data class Call(
    val email: String,
    val videoManager: P2PManagerImpl,
    val audioManager: P2PManagerImpl,
    val isMyCall: Boolean = true
)

sealed class CallState {
    object NoCall : CallState()
    object Calling : CallState()
    object AcceptedCall : CallState()
    object Cancelled : CallState()
}

object Repository {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    var currentCall = mutableStateOf<Call?>(null)
    val currentCallState = MutableStateFlow<CallState>(CallState.NoCall)
    val pomegranatePath by lazy { "$rootDirectory${separator}pomegranate$separator" }
    val fistFilePath by lazy { "${pomegranatePath}auth.txt" }
    val myEmail by lazy {
        while (KMPFile(fistFilePath).kmpReadText() == "") {

        }
        KMPFile(fistFilePath).kmpReadText()
    }

    init {
        if (KMPFile(fistFilePath).exists()) {
            KMPFile("$pomegranatePath${separator}temp").mkdir()

        } else {
            if (KMPFile(pomegranatePath).exists()) {
                KMPFile("$pomegranatePath${separator}temp").mkdir()
                KMPFile(fistFilePath).createNewFile()
            } else {
                KMPFile(pomegranatePath).mkdir()
                KMPFile("$pomegranatePath${separator}temp").mkdir()
                KMPFile(fistFilePath).createNewFile()
            }
        }
    }

    lateinit var messagesDao: MessagesDao
    lateinit var chatDao: ChatDao
    lateinit var personsDao: PersonDao
    private val _lastChat = MutableStateFlow<ChatDC?>(null)
    val lastChat: StateFlow<ChatDC?> = _lastChat
    var lastEmail = ""

    val availablePersons = mutableMapOf<String, MutableSharedFlow<Observer?>>()

    val waitedConnection = mutableMapOf<String, MutableList<MessageDC>>()

    init {
        scope.launch {
            while (true) {
                waitedConnection.forEach { (email, messages) ->
                    try {
                        val manager = BaseP2P().createConnection(email)
                        val observer = Observer(
                            manager,
                            manager.channel!!,
                            email,
                            messagesDao
                        )
                        availablePersons.getOrPut(email, { MutableSharedFlow(1) }).emit(observer)
                        messages.forEach { message ->
                            observer.sendMessage(message)
                        }
                        waitedConnection.remove(email)
                    } catch (_: TimeoutCancellationException) {
                    }
                }
                delay(3000.milliseconds)
            }
        }
    }

    fun setLastChat(chat: ChatDC?) {
        _lastChat.value = chat
    }

    fun createMessage(
        chatDC: ChatDC,
        message: String? = null,
        file: KMPFile? = null,
        type: String
    ): MessageDC {
        var currentMessage: MessageDC? = null
        val time = now().toEpochMilliseconds()
        when (type) {
            MessageDC.TEXT -> {
                val messageDC = MessageDC(
                    messageCreator = myEmail,
                    data = message!!.encodeToByteArray(),
                    type = MessageDC.TEXT,
                    time = time,
                    isMine = true,
                    chatName = chatDC.chatName,
                    chatType = chatDC.chatType,
                    chatCreator = chatDC.chatCreator,
                )
                currentMessage = messageDC
            }

            MessageDC.BEGIN_CALL, MessageDC.ACCEPT_CALL -> {
                val messageDC = MessageDC(
                    messageCreator = myEmail,
                    data = ByteArray(0),
                    type = type,
                    time = time,
                    isMine = true,
                    chatName = chatDC.chatName,
                    chatType = chatDC.chatType,
                    chatCreator = chatDC.chatCreator,
                )
                currentMessage = messageDC
            }

            MessageDC.SECURITY_CONFIG -> {
                val cfg = AppSettings.state.value.securityConfigUnknown
                println(cfg.toString())
                val messageDC = MessageDC(
                    messageCreator = myEmail,
                    data = cfg.toString().toByteArray(),
                    type = type,
                    time = time,
                    isMine = true,
                    chatName = chatDC.chatName,
                    chatType = chatDC.chatType,
                    chatCreator = chatDC.chatCreator,
                )
                currentMessage = messageDC
            }

            else -> {
                if (file != null) {
                    val type = when (file.getName().substringAfter(".")) {
                        "png", "jpg", "jpeg" -> MessageDC.IMAGE
                        "gif", "webp" -> MessageDC.ANIMATED_IMAGE
                        "ogg", "mp3", "wav" -> MessageDC.AUDIO
                        else -> MessageDC.FILE
                    }
                    val messageDC = MessageDC(
                        messageCreator = myEmail,
                        data = file.getAbsolutePath().encodeToByteArray(),
                        type = type,
                        time = time,
                        isMine = true,
                        chatName = chatDC.chatName,
                        chatType = chatDC.chatType,
                        chatCreator = chatDC.chatCreator,
                    )
                    currentMessage = messageDC
                }
            }
        }
        return currentMessage!!
    }
}