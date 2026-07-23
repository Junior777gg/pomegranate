package org.unstabledev.pomegranate.P2PUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.unstabledev.pomegranate.Notifications
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val timeOutMillis = 300000L
    var lastAction = now().toEpochMilliseconds()
    val deliverMap = mutableMapOf<Byte, ByteArray>()

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
                availableChats.getOrPut(chatDC, { MutableSharedFlow(1) }).emit(null)
            }
        }
    }

    private fun receive() {
        scope.launch {
            try {
                val flow = MutableSharedFlow<ByteArray>(2)
                launch {
                    while (true) {
                        val data = channel.receive()
                        lastAction = now().toEpochMilliseconds()
                        flow.emit(data)
                    }
                }
                launch {
                    val map = mutableMapOf<Byte, MutableList<ByteArray>>()
                    launch {
                        flow.collect {
                            if (it.size == 1) {
                                val message = messagesDao.getByData(deliverMap[it[0]]!!)
                                message.isDelivered = true
                                messagesDao.upsertMessage(message)
                                deliverMap.remove(it[0])
                            } else {
                                val code = it.last()
                                map.getOrPut(code, { mutableListOf() })
                                    .add(it.copyOfRange(0, it.size - 1))
                            }
                        }
                    }
                    launch {
                        while (true) {
                            map.keys.forEach { key ->
                                if (map[key]!!.size == 2) {
                                    val list = map[key]!!
                                    val messageDC = try {
                                        val json =
                                            Json.decodeFromString(MessageDC.serializer(), list[0].decodeToString())
                                        json.data = list[1]
                                        json
                                    } catch (_: SerializationException) {
                                        val json =
                                            Json.decodeFromString(MessageDC.serializer(), list[1].decodeToString())
                                        json.data = list[0]
                                        json
                                    }
                                    messageDC.isMine = false
                                    messageDC.email = chatDC.partnerEmail
                                    messageDC.isDelivered = true
                                    sendCode(key)
                                    Notifications().push(
                                        (chatDC.profile?.deserialize()?.displayName ?: chatDC.partnerEmail),
                                        when(messageDC.type) {
                                            MessageDC.TEXT->messageDC.data.decodeToString().stripMarkdown()
                                            MessageDC.IMAGE->"🖼 Изображение"
                                            MessageDC.FILE->"📁 Файл"
                                            else -> "Unknown"
                                        }
                                    )
                                    messagesDao.insertMessage(messageDC)
                                    map.remove(key)
                                }
                            }
                            delay(100)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(message: MessageDC) {
        lastAction = now().toEpochMilliseconds()
        scope.launch {
            val data = message.data
            val msg = message.copy(data = ByteArray(0))
            val code = Random.nextInt(1, 255).toByte()
            deliverMap[code] = data
            val json = Json.encodeToString(msg)
            channel.send(json.encodeToByteArray() + code)
            channel.send(data + code)
        }
    }

    fun sendCode(code: Byte) {
        scope.launch {
            channel.send(byteArrayOf(code))
        }
    }
}