package org.unstabledev.pomegranate.P2PUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.database.MessagesDao
import kotlin.time.Clock.System.now

class Observer(private val channel: P2PChannelImpl, val chatDC: ChatDC, val messagesDao: MessagesDao){
    private var itsReceived = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val messages = MutableSharedFlow<MessageDC>(16)
    init {
        receive()
    }
    private fun receive(){
        if(itsReceived) {return}
        scope.launch{
            itsReceived = true
            while(true){
                val time = now().toString().split("T")[1].split(":")
                val data = channel.receive()
                val message = MessageDC(
                    email = chatDC.partnerEmail,
                    data = data,
                    type = MessageDC.TEXT,
                    time = "${time[0].toInt() + 3}:${time[1]}",
                    isMine = false,
                )
                messagesDao.insertMessage(message)
                messages.emit(message)
            }
        }
    }

    fun send(message: String){
        scope.launch{
            channel.send(message.encodeToByteArray())
        }
    }
}