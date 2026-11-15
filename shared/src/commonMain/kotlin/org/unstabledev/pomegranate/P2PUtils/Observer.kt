package org.unstabledev.pomegranate.P2PUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.unstabledev.pomegranate.Call
import org.unstabledev.pomegranate.CallState
import org.unstabledev.pomegranate.platform.KMPFile
import org.unstabledev.pomegranate.platform.Notifications
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Repository.currentCall
import org.unstabledev.pomegranate.Repository.currentCallState
import org.unstabledev.pomegranate.Repository.pomegranatePath
import org.unstabledev.pomegranate.common.AppSettings
import org.unstabledev.pomegranate.common.AppTracer
import org.unstabledev.pomegranate.common.SecurityConfig
import org.unstabledev.pomegranate.common.Util.Companion.stripMarkdown
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.MessageDC.Companion.isDisplayable
import org.unstabledev.pomegranate.database.MessageDC.Companion.truncatedMessageDCType
import org.unstabledev.pomegranate.platform.kmpCopyTo
import kotlin.random.Random
import kotlin.time.Clock.System.now
import kotlin.time.Duration.Companion.milliseconds

class Observer(
    private val manager: P2PManager,
    private val channel: P2PChannel,
    val email: String,
) {
    val messagesDao = Repository.messagesDao
    val chatDao = Repository.chatDao
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val timeOutMillis = 300000L
    var lastAction = now().toEpochMilliseconds()
    val deliverMap = mutableMapOf<String, Long>()

    init {
        receive()
        CoroutineScope(Dispatchers.IO).launch {
            while ((timeOutMillis + lastAction > now().toEpochMilliseconds()) || currentCall.value != null) {
                delay(5000.milliseconds)
            }
            try {
                scope.cancel()
                manager.breakConnection()
            } finally {
                Repository.availablePersons.getOrPut(email) { MutableSharedFlow(1) }.emit(null)
            }
        }
    }

    private fun receive() {
        scope.launch {
            channel.receiver.collect {
                lastAction = now().toEpochMilliseconds()
                try {
                    if (it.size == 1 && it[0] is Text && deliverMap[(it[0] as Text).text] != null) {
                        val messageDC = messagesDao.getByTime((it[0] as Text).text.toLong())
                        messageDC.isDelivered = true
                        messagesDao.upsert(messageDC)
                    } else {
                        val messageDC = Json.decodeFromString<MessageDC>((it[0] as Text).text)
                        val data = it[1]
                        when (data) {
                            is Bytes -> {
                                messageDC.data = data.bytes
                            }

                            is Files -> {
                                val subDir = truncatedMessageDCType(messageDC.type)
                                val targetDir = "${pomegranatePath}temp${if (subDir == null) "" else "/$subDir"}"
                                val nameFile = KMPFile(targetDir, messageDC.data.decodeToString())
                                nameFile.getParentFile()?.mkdirs()
                                data.file.kmpCopyTo(nameFile)
                                data.file.delete()
                                messageDC.data = nameFile.getAbsolutePath().encodeToByteArray()
                            }
                        }
                        sendCode((it[2] as Text).text)
                        val personDC = Repository.personsDao.getByEmail(email)
                        val chatDC =
                            Repository.chatDao.tryGet(messageDC.chatName, messageDC.chatCreator).first()
                        if (messageDC.type != MessageDC.ACCEPT_CALL && messageDC.isDisplayable()
                            && !AppSettings.isMuted(chatDC?.key)
                        ) {
                            Notifications().push(
                                (personDC?.nickname ?: personDC?.personEmail ?: ""),
                                when (messageDC.type) {
                                    MessageDC.TEXT -> messageDC.data.decodeToString()
                                        .stripMarkdown()

                                    MessageDC.BEGIN_CALL, MessageDC.ACCEPT_CALL -> "📞 Звонок"
                                    MessageDC.IMAGE, MessageDC.ANIMATED_IMAGE -> "🖼 Изображение"
                                    MessageDC.AUDIO -> "🎵 Аудио"
                                    MessageDC.FILE -> "📁 Файл"
                                    else -> "Неизвестно"
                                }
                            )
                        }
                        if (messageDC.chatType == ChatDC.Companion.ChatTypes.CHAT) {
                            messageDC.chatCreator = personDC?.personEmail ?: "null"
                            val name = Repository.chatDao.getName(messageDC.chatCreator, messageDC.chatType)
                            if (name == null) {
                                val newChat = ChatDC(
                                    chatName = email,
                                    chatCreator = email,
                                    chatType = messageDC.chatType,
                                    personsEmails = mutableListOf(email)
                                )
                                chatDao.upsert(newChat)
                                messageDC.chatName = newChat.chatName
                            }
                        }
                        val isCall = messageDC.type == MessageDC.BEGIN_CALL
                        if (isCall) {
                            val videoManager = manager.fork()
                            val audioManager = manager.fork()
                            currentCallState.value = CallState.Calling
                            currentCall.value =
                                Call(personDC?.personEmail!!, videoManager!!, audioManager!!, false)
                        }
                        if (messageDC.type == MessageDC.INVITE) {
                            Repository.chatDao.upsert(
                                ChatDC(
                                    chatName = messageDC.chatName,
                                    chatCreator = messageDC.chatCreator,
                                    chatType = messageDC.chatType,
                                    personsEmails = Json.decodeFromString(messageDC.supData.decodeToString())
                                )
                            )
                        }
                        if (messageDC.type == MessageDC.SECURITY_CONFIG) {
                            val cfg = SecurityConfig().fromByteArray(messageDC.data)
                            personDC?.securityConfig = cfg.toString()
                            if (personDC != null) Repository.personsDao.upsert(personDC)
                        }
                        messageDC.isMine = false
                        messageDC.isDelivered = true
                        messagesDao.insert(messageDC)
                        AppTracer.trace("recieve:$email")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun sendMessage(message: MessageDC) {
        lastAction = now().toEpochMilliseconds()
        scope.launch {
            val data = message.data
            val msg = message.copy()
            val code = Random.nextInt(1, 65000).toString()
            val isCall = message.type == MessageDC.BEGIN_CALL
            if (isCall) {
                launch {
                    val videoManager = manager.fork()
                    val audioManager = manager.fork()
                    currentCallState.value = CallState.AcceptedCall
                    currentCall.value = Call(email, videoManager!!, audioManager!!)
                }
            }
            deliverMap[code] = msg.time
            if (message.type != MessageDC.TEXT && !isCall) {
                msg.data = KMPFile(msg.data.decodeToString()).getName().encodeToByteArray()
            }
            val json = Json.encodeToString(msg)
            if (message.type == MessageDC.TEXT || isCall || message.type == MessageDC.INVITE) {
                channel.send(json, data, code)
            } else {
                val dataFile = KMPFile(data.decodeToString())
                channel.send(json, dataFile, code)
            }
            AppTracer.trace(
                "${if (message.chatType == ChatDC.Companion.ChatTypes.GROUP) "send_group" else "send"}:" +
                        "${message.chatName}_${message.chatCreator}"
            )
        }
    }

    fun sendCode(code: String) {
        scope.launch {
            channel.send(code)
        }
    }
}