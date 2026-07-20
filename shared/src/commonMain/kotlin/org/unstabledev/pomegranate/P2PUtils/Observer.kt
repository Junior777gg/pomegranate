package org.unstabledev.pomegranate.P2PUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.Notifications
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Repository.availableChats
import org.unstabledev.pomegranate.Util.Companion.stripMarkdown
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.MessagesDao
import org.unstabledev.pomegranate.database.deserialize
import kotlin.random.Random
import kotlin.time.Clock.System.now

class Observer(
    private val manager: P2PManagerImpl,
    private val channel: P2PChannelImpl,
    val chatDC: ChatDC,
    val messagesDao: MessagesDao
) {
    private var itsReceived = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val timeOutMillis = 15000L
    var lastAction = now().toEpochMilliseconds()
    var lastData = ByteArray(0)
    var code = ""

    init {
        receive()
        CoroutineScope(Dispatchers.IO).launch {
            while (timeOutMillis + lastAction > now().toEpochMilliseconds()) {
                delay(5000)
            }
            try {
                scope.cancel()
                manager.breakConnection()
            } finally {
                availableChats.getOrPut(chatDC, {MutableSharedFlow(1)}).emit(null)
            }
        }
    }

    private fun receive() {
        scope.launch {
            try {
                if (itsReceived) return@launch
                itsReceived = true
                while (true) {
                    val time = now().toString().split("T")[1].split(":")
                    val data = channel.receive()
                    if (data.decodeToString() == code) {
                        val message = messagesDao.getByData(lastData)
                        message.isDelivered = true
                        messagesDao.upsertMessage(message)
                        lastData = ByteArray(0)
                        code = ""
                    } else {
                        val decodeData = data.decodeToString().split("^?^/^*")
                        send(decodeData[1], true)
                        lastAction = now().toEpochMilliseconds()
                        val message = MessageDC(
                            email = chatDC.partnerEmail,
                            data = decodeData[0].encodeToByteArray(),
                            type = MessageDC.TEXT,
                            time = "${time[0].toInt() + 3}:${time[1]}",
                            isMine = false,
                        )
                        Notifications().push(
                            (chatDC.profile?.deserialize()?.displayName ?: chatDC.partnerEmail),
                            decodeData[0].stripMarkdown()
                        )
                        messagesDao.insertMessage(message)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun send(message: String, isTest: Boolean = false) {
        lastAction = now().toEpochMilliseconds()
        scope.launch {
            if (isTest) {
                channel.send(message.encodeToByteArray())
            } else {
                lastData = message.encodeToByteArray()
                code = Random.nextInt().toString()
                channel.send("$message^?^/^*$code".encodeToByteArray())
            }
        }
    }
}