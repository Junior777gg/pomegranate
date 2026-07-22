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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    val timeOutMillis = 120000L
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
                    val data = channel.receive()
                    if (data.decodeToString() == code) {
                        val message = messagesDao.getByData(lastData)
                        message.isDelivered = true
                        messagesDao.upsertMessage(message)
                        lastData = ByteArray(0)
                        code = ""
                    } else {
                        val decodeData = data.decodeToString().split("^?^/^*")
                        send(MessageDC(data = decodeData[1].encodeToByteArray()), true)
                        lastAction = now().toEpochMilliseconds()
                        val message = Json.decodeFromString(MessageDC.serializer(), decodeData[0])
                        message.isMine = false
                        message.email = chatDC.partnerEmail
                        Notifications().push(
                            (chatDC.profile?.deserialize()?.displayName ?: chatDC.partnerEmail),
                            when(message.type) {
                                MessageDC.TEXT->message.data.decodeToString().stripMarkdown()
                                MessageDC.IMAGE->"🖼 Изображение"
                                MessageDC.FILE->"📁 Файл"
                                else -> "Unknown"
                            }
                        )
                        messagesDao.insertMessage(message)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun send(message: MessageDC, isTest: Boolean = false) {
        lastAction = now().toEpochMilliseconds()
        scope.launch {
            if (isTest) {
                channel.send(message.data)
            } else {
                val json = Json.encodeToString(message)
                lastData = message.data
                code = Random.nextInt().toString()
                channel.send("$json^?^/^*$code".encodeToByteArray())
            }
        }
    }
}