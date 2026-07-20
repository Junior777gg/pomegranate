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
import org.unstabledev.pomegranate.database.MessagesDao

object Repository {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val fistFilePath = "pomegranate${File.sep}auth.txt"
    val myEmail by lazy { File(fistFilePath).readText() }
    lateinit var messagesDao: MessagesDao

    private val _lastContact = MutableStateFlow<ChatDC?>(null)
    val lastContact: StateFlow<ChatDC?> = _lastContact

    val availableChats = mutableMapOf<ChatDC,MutableSharedFlow<Observer?>>()
    var lastOpponentEmail = ""

    val waitedConnection = mutableMapOf<ChatDC, MutableList<String>>()

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
                        availableChats.getOrPut(chatDC, {MutableSharedFlow(1)}).emit(observer)
                        messages.forEach { message ->
                            observer.send(message)
                        }
                        waitedConnection.remove(chatDC)
                    }catch (_: TimeoutCancellationException){

                    }
                }
                delay(3000)
            }
        }
    }

    fun setLastContact(contact: ChatDC?) {
        _lastContact.value = contact
    }
}