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
import org.unstabledev.pomegranate.KMPFile
import org.unstabledev.pomegranate.Notifications
import org.unstabledev.pomegranate.Repository.availableChats
import org.unstabledev.pomegranate.Repository.pomegranatePath
import org.unstabledev.pomegranate.Util.Companion.stripMarkdown
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.MessagesDao
import org.unstabledev.pomegranate.database.deserialize
import org.unstabledev.pomegranate.kmpCopyTo
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
                val flow = MutableSharedFlow<Data>(100)
                launch {
                    while (true) {
                        val data = channel.receive()
                        lastAction = now().toEpochMilliseconds()
                        flow.emit(data)
                    }
                }
                launch {
                    val map = mutableMapOf<Byte, MutableList<Data>>()
                    launch {
                        flow.collect {
                            if (it is Data.Bytes && it.bytes.size == 1 && deliverMap[it.bytes[0]]!=null) {
                                val buffer = it.bytes
                                val message = messagesDao.getByData(deliverMap[buffer[0]]!!)
                                if (message != null) {
                                    message.isDelivered = true
                                    messagesDao.upsertMessage(message)
                                }
                                deliverMap.remove(buffer[0])
                            } else {
                                when (it) {
                                    is Data.Bytes -> map.getOrPut(it.code, { mutableListOf() })
                                        .add(it)

                                    is Data.Files -> map.getOrPut(it.code, { mutableListOf() })
                                        .add(it)
                                }
                            }
                        }
                    }
                    launch {
                        while (true) {
                            map.keys.toList().forEach { key ->
                                if (map[key]!!.size == 2) {
                                    val list = map[key]!!
                                    val messageDC = try {
                                        if (list[0] is Data.Bytes && list[1] is Data.Files) {
                                            val json =
                                                Json.decodeFromString<MessageDC>((list[0] as Data.Bytes).bytes.decodeToString())
                                            val file = (list[1] as Data.Files).file
                                            val nameFile = KMPFile("${pomegranatePath}temp", json.data.decodeToString())
                                            file.kmpCopyTo(nameFile)
                                            file.delete()
                                            json.data = nameFile.getAbsolutePath().encodeToByteArray()
                                            json
                                        } else if (list[1] is Data.Bytes && list[0] is Data.Files) {
                                            val json =
                                                Json.decodeFromString<MessageDC>((list[1] as Data.Bytes).bytes.decodeToString())
                                            val file = (list[0] as Data.Files).file
                                            val nameFile = KMPFile("${pomegranatePath}temp", json.data.decodeToString())
                                            file.kmpCopyTo(nameFile)
                                            file.delete()
                                            json.data = nameFile.getAbsolutePath().encodeToByteArray()
                                            json
                                        } else {
                                            val json =
                                                Json.decodeFromString<MessageDC>((list[0] as Data.Bytes).bytes.decodeToString())
                                            json.data = (list[1] as Data.Bytes).bytes
                                            json
                                        }
                                    } catch (_: SerializationException) {
                                        val json =
                                            Json.decodeFromString<MessageDC>((list[1] as Data.Bytes).bytes.decodeToString())
                                        json.data = (list[0] as Data.Bytes).bytes
                                        json
                                    }
                                    messageDC.isMine = false
                                    messageDC.email = chatDC.partnerEmail
                                    messageDC.isDelivered = true
                                    sendCode(key)
                                    Notifications().push(
                                        (chatDC.profile?.deserialize()?.displayName ?: chatDC.partnerEmail),
                                        when (messageDC.type) {
                                            MessageDC.TEXT -> messageDC.data.decodeToString().stripMarkdown()
                                            MessageDC.CALL -> "📞 Звонок"
                                            MessageDC.IMAGE -> "🖼 Изображение"
                                            MessageDC.ANIMATED_IMAGE -> "🖼 Изображение"
                                            MessageDC.AUDIO -> "🎵 Аудио"
                                            MessageDC.FILE -> "📁 Файл"
                                            else -> "Неизвестно"
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
            val msg = message.copy()
            val code = Random.nextInt(1, 255).toByte()
            deliverMap[code] = data
            if (message.type != MessageDC.TEXT) {
                msg.data = KMPFile(msg.data.decodeToString()).getName().encodeToByteArray()
            }else{
                println("input from Observer ${data.decodeToString()}")
            }
            val json = Json.encodeToString(msg).encodeToByteArray()
            channel.send(json, code)
            if (message.type == MessageDC.TEXT || message.type == MessageDC.CALL) {
                channel.send(data, code)
            } else {
                val dataFile = KMPFile(data.decodeToString())
                channel.send(dataFile, code)
            }

        }
    }

    fun sendCode(code: Byte) {
        scope.launch {
            channel.send(byteArrayOf(code))
        }
    }
}