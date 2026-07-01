package org.unstabledev.pomegranate.P2PUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class Observer(private val channel: P2PChannelImpl){
    private var itsReceived = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val messages = MutableSharedFlow<String>(16)
    init {
        receive()
    }
    private fun receive(){
        if(itsReceived) {return}
        scope.launch{
            itsReceived = true
            while(true){
                messages.emit(channel.receive().decodeToString())
            }
        }
    }

    fun send(message: String){
        scope.launch{
            channel.send(message.encodeToByteArray())
        }
    }
}