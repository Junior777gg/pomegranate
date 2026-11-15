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
import kotlinx.coroutines.withContext
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
import org.unstabledev.pomegranate.database.PersonDC
import org.unstabledev.pomegranate.platform.kmpCopyTo
import org.unstabledev.pomegranate.screen.Profile
import kotlin.random.Random
import kotlin.time.Clock.System.now
import kotlin.time.Duration.Companion.milliseconds

class Observer(
    private val manager: P2PManager,
    private val channel: P2PChannel,
    val personEmail: String,
) {
    val messagesDao = Repository.messagesDao
    val chatDao = Repository.chatDao
    val personDao = Repository.personsDao
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
                Repository.availablePersons.getOrPut(personEmail) { MutableSharedFlow(1) }.emit(null)
            }
        }
    }

    private fun receive() {
        scope.launch {
            channel.receiver.collect {
                lastAction = now().toEpochMilliseconds()
                try {
                    if (it.size == 1 && it[0] is Text && deliverMap[(it[0] as Text).text] != null) {
                        val messageDC = messagesDao.getByTime(deliverMap[(it[0] as Text).text]!!)
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

                        val chatDC = getChat(personEmail, messageDC)
                        messageDC.apply {
                            chatName = chatDC.chatName
                            chatCreator = chatDC.chatCreator
                        }
                        if (messageDC.type != MessageDC.ACCEPT_CALL && messageDC.isDisplayable()
                            && !AppSettings.isMuted(chatDC.key)
                        ) {
                            Notifications().push(
                                (chatDC.chatName),
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
                        val isCall = messageDC.type == MessageDC.BEGIN_CALL
                        if (isCall) {
                            val videoManager = manager.fork()
                            val audioManager = manager.fork()
                            currentCallState.value = CallState.Calling
                            currentCall.value =
                                Call(personEmail, videoManager!!, audioManager!!, false)
                        }
                        if (messageDC.type == MessageDC.SECURITY_CONFIG) {
                            val cfg = SecurityConfig().fromByteArray(messageDC.data)
                            /*personDC?.securityConfig = cfg.toString()
                            if (personDC != null) Repository.personsDao.upsert(personDC)*/
                        }
                        messageDC.isMine = false
                        messageDC.isDelivered = true
                        messagesDao.insert(messageDC)
                        AppTracer.trace("recieve:$personEmail")
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
                    currentCall.value = Call(personEmail, videoManager!!, audioManager!!)
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

    suspend fun getChat(emil: String, message: MessageDC): ChatDC {
        var chat = chatDao.tryGet(message.chatType, emil).first()
        if (chat != null) {
            return chat
        }
        val person = getPerson(emil)
        chat = ChatDC(
            chatCreator = emil,
            chatType = message.chatType,
            chatName = person.nickname ?: try {
                Json.decodeFromString<Profile?>(person.profile!!)?.displayName!!
            } catch (_: Exception) {
                emil
            },
            personsEmails = mutableListOf(emil)
        )
        chatDao.upsert(chat)
        return chat
    }

    suspend fun getPerson(email: String): PersonDC {
        var person = personDao.tryGetByEmail(email).first()
        if (person != null) {
            return person
        }
        person = PersonDC(email)
        personDao.insert(person)
        return person
    }
}