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
import org.unstabledev.pomegranate.database.MessageDC.Companion.description
import org.unstabledev.pomegranate.database.MessageDC.Companion.isDisplayable
import org.unstabledev.pomegranate.database.MessageDC.Companion.truncatedMessageDCType
import org.unstabledev.pomegranate.database.PersonDC
import org.unstabledev.pomegranate.platform.kmpCopyTo
import org.unstabledev.pomegranate.screen.Profile
import kotlin.random.Random
import kotlin.time.Clock.System.now
import kotlin.time.Duration.Companion.milliseconds

class Observer(
    val manager: P2PManager,
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
            launch {
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
    }

    private fun receive() {
        scope.launch {
            channel.receiver.collect {
                lastAction = now().toEpochMilliseconds()
                try {
                    if (it.size == 1 && it[0] is Text && deliverMap[(it[0] as Text).text] != null) {
                        val ackCode = (it[0] as Text).text
                        val targetTime = deliverMap.remove(ackCode)
                        if (targetTime != null) {
                            val messageDC = messagesDao.getByTime(targetTime)
                            if (messageDC != null) {
                                messageDC.isDelivered = true
                                messagesDao.upsert(messageDC)
                            }
                        }
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
                        var canInsertMessage = true
                        messageDC.apply {
                            chatName = chatDC.chatName
                            chatCreator = chatDC.chatCreator
                        }

                        val isCall = messageDC.type == MessageDC.BEGIN_CALL
                        if (isCall) {
                            val videoManager = manager.fork(false)
                            val audioManager = manager.fork(false)
                            currentCallState.value = CallState.Calling
                            currentCall.value =
                                Call(personEmail, videoManager!!, audioManager!!, false)
                        }
                        if (messageDC.type == MessageDC.SECURITY_CONFIG) {
                            val cfg = SecurityConfig().fromByteArray(messageDC.data)
                            /*personDC?.securityConfig = cfg.toString()
                            if (personDC != null) Repository.personsDao.upsert(personDC)*/
                            canInsertMessage = false
                        }
                        if (messageDC.type == MessageDC.MSG_DELETE) {
                            try {
                                val targetTime = messageDC.data.decodeToString().toLong()
                                val msg = Repository.messagesDao.getByTime(targetTime)
                                if (msg != null) Repository.messagesDao.delete(msg)
                            } catch (_: Exception) { }
                            canInsertMessage = false
                        }
                        if (messageDC.type == MessageDC.MSG_PIN) {
                            try {
                                val cont = messageDC.data.decodeToString().split(":")
                                val targetTime = cont[1].toLong()
                                val pin = cont[0].toBoolean()
                                val msg = Repository.messagesDao.getByTime(targetTime)
                                if (msg != null) {
                                    val copy=chatDC.pinnedMessages.toMutableList()
                                    chatDC.pinnedMessages.add(msg.key)
                                    if(pin) copy.add(msg.key)
                                    else copy.remove(msg.key)
                                    scope.launch { Repository.chatDao.upsert(chatDC.copy(pinnedMessages = copy)) }
                                }
                            } catch (_: Exception) { }
                            canInsertMessage = false
                        }

                        if (canInsertMessage && messageDC.type != MessageDC.ACCEPT_CALL && messageDC.isDisplayable()
                            && !AppSettings.isMuted(chatDC.key)) {
                            Notifications().push(chatDC.chatName, messageDC.description())
                        }

                        messageDC.isMine = false
                        messageDC.isDelivered = true
                        if (canInsertMessage) messagesDao.insert(messageDC)
                        AppTracer.trace("receive:$personEmail")
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
                    val videoManager = manager.fork(false)
                    val audioManager = manager.fork(false)
                    currentCallState.value = CallState.AcceptedCall
                    currentCall.value = Call(personEmail, videoManager!!, audioManager!!)
                }
            }
            deliverMap[code] = msg.time
            val isRawData = message.type == MessageDC.TEXT ||
                    message.type == MessageDC.MSG_DELETE ||
                    message.type == MessageDC.MSG_PIN ||
                    message.type == MessageDC.INVITE ||
                    message.type == MessageDC.SECURITY_CONFIG ||
                    isCall
            if (!isRawData) {
                msg.data = KMPFile(msg.data.decodeToString()).getName().encodeToByteArray()
            }
            val json = Json.encodeToString(msg)
            if (isRawData) {
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

    suspend fun getChat(emil: String, message: MessageDC): ChatDC{
        var chat = chatDao.tryGet(message.chatType, emil).first()
        if (chat != null) {
            return chat
        }
        if (message.chatType == ChatDC.Companion.ChatTypes.CHAT) {
            val person = getPerson(emil)
            chat = ChatDC(
                chatCreator = emil,
                chatType = message.chatType,
                chatName = person.nickname ?: try {
                    Json.decodeFromString<Profile?>(person.profile!!)?.displayName!!
                } catch (_: Exception) {
                    emil
                },
                personsEmails = mutableListOf(emil),
                pinnedMessages = mutableListOf()
            )
            chatDao.upsert(chat)
            return chat
        }else {
            return ChatDC(
                chatName = message.chatName,
                chatCreator = message.chatCreator,
                chatType = message.chatType,
                personsEmails = Json.decodeFromString(message.supData.decodeToString()),
                pinnedMessages = mutableListOf()
            )
        }
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