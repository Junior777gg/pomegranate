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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.unstabledev.pomegranate.P2PUtils.Observer
import org.unstabledev.pomegranate.P2PUtils.P2PManager
import org.unstabledev.pomegranate.common.AppSettings
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.MessagesDao
import org.unstabledev.pomegranate.database.PersonDao
import org.unstabledev.pomegranate.platform.KMPFile
import org.unstabledev.pomegranate.platform.kmpReadText
import org.unstabledev.pomegranate.platform.rootDirectory
import org.unstabledev.pomegranate.platform.separator
import kotlin.collections.chunked
import kotlin.collections.first
import kotlin.getValue
import kotlin.time.Clock.System.now
import kotlin.time.Duration.Companion.milliseconds

data class Call(
    val email: String,
    val videoManager: P2PManager,
    val audioManager: P2PManager,
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
    val pomegranatePath by lazy { "${rootDirectory}${separator}pomegranate${separator}" }
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

    fun startMessaging(email: String, messages: MutableList<MessageDC>) {
        scope.launch(Dispatchers.IO) {
            val manager = BaseP2P().createConnection(email)
            try {
                val observer = Observer(
                    manager,
                    manager.channel!!,
                    email,
                )
                availablePersons.getOrPut(email) { MutableSharedFlow(1) }.emit(observer)
                messages.forEach {
                    observer.sendMessage(it)
                }
            } catch (_: TimeoutCancellationException) {
                if (waitedConnection[email] == null) {
                    waitedConnection[email] = messages
                } else {
                    waitedConnection[email]!!.addAll(messages)
                }
            }
        }
    }

    suspend fun sendInGroup(clients: MutableList<String>, messagesList: MutableList<MessageDC>) = withContext(Dispatchers.IO) {
        clients.remove(myEmail)
        println(clients)
        if (clients.isEmpty()) {return@withContext}
        if (clients.size == 1) {
            val observer = availablePersons[clients.first()]?.first()
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
                startMessaging(clients.first(), messagesList)
            }
            return@withContext
        }
        val pair = if (clients.size % 2 == 0) {
            clients.chunked(clients.size / 2)
        } else {
            clients.chunked((clients.size - 1) / 2)
        }
        for (each in pair) {
            val observer = availablePersons[each.first()]?.first()
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
                startMessaging(each.first(), messagesList)
            }
        }
    }

    fun createMessage(
        chatDC: ChatDC,
        message: String? = null,
        file: KMPFile? = null,
        supData: ByteArray = byteArrayOf(),
        type: String,
        bulkId: Long = 0
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
                    supData = supData,
                    bulkId = bulkId,
                )
                currentMessage = messageDC
            }

            MessageDC.BEGIN_CALL, MessageDC.ACCEPT_CALL, MessageDC.INVITE -> {
                val messageDC = MessageDC(
                    messageCreator = myEmail,
                    data = "some symbols".encodeToByteArray(),
                    type = type,
                    time = time,
                    isMine = true,
                    chatName = chatDC.chatName,
                    chatType = chatDC.chatType,
                    chatCreator = chatDC.chatCreator,
                    supData = supData,
                    bulkId = bulkId,
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
                    supData = supData,
                    bulkId = bulkId,
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
                        supData = supData,
                        bulkId = bulkId,
                    )
                    currentMessage = messageDC
                }
            }
        }
        return currentMessage!!
    }
}