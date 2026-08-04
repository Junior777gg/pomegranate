package org.unstabledev.pomegranate

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
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.MessagesDao
import kotlin.getValue
import kotlin.time.Clock.System.now

object Repository {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val pomegranatePath by lazy { "$rootDirectory${separator}pomegranate$separator" }
    val fistFilePath by lazy { "${pomegranatePath}auth.txt" }

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

    val myEmail by lazy { KMPFile(fistFilePath).readText() }
    lateinit var messagesDao: MessagesDao
    private val _lastContact = MutableStateFlow<ChatDC?>(null)
    val lastContact: StateFlow<ChatDC?> = _lastContact

    val availableChats = mutableMapOf<ChatDC, MutableSharedFlow<Observer?>>()
    var lastOpponentEmail = ""

    val waitedConnection = mutableMapOf<ChatDC, MutableList<MessageDC>>()

    init {
        scope.launch {
            while (true) {
                waitedConnection.forEach { (chatDC, messages) ->
                    try {
                        val manager = BaseP2P().createConnection(chatDC.partnerEmail)
                        val observer = Observer(
                            manager,
                            manager.channel!!,
                            chatDC,
                            messagesDao
                        )
                        availableChats.getOrPut(chatDC, { MutableSharedFlow(1) }).emit(observer)
                        messages.forEach { message ->
                            observer.sendMessage(message)
                        }
                        waitedConnection.remove(chatDC)
                    } catch (_: TimeoutCancellationException) {

                    }
                }
                delay(3000)
            }
        }
    }

    fun setLastContact(contact: ChatDC?) {
        _lastContact.value = contact
    }

    fun createMessage(chatDC: ChatDC, message: String? = null, file: KMPFile? = null): MessageDC {
        var currentMessage: MessageDC? = null
        if (message != null) {
            val time = now().toString().split("T")[1].split(":")
            val messageDC = MessageDC(
                email = chatDC.partnerEmail,
                data = message.encodeToByteArray(),
                type = MessageDC.TEXT,
                time = "${time[0].toInt() + 3}:${time[1]}",
                isMine = true,
            )
            currentMessage = messageDC
        }
        if (file != null) {
            val time = now().toString().split("T")[1].split(":")
            val type = when (file.getName().substringAfter(".")) {
                "png", "jpg", "jpeg" -> MessageDC.IMAGE
                "gif", "webp" -> MessageDC.ANIMATED_IMAGE
                else -> MessageDC.FILE
            }
            val messageDC = MessageDC(
                email = chatDC.partnerEmail,
                data = file.getAbsolutePath().encodeToByteArray(),
                type = type,
                time = "${time[0].toInt() + 3}:${time[1]}",
                isMine = true,
            )
            currentMessage = messageDC
        }
        return currentMessage!!
    }
}