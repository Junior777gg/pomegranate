package org.unstabledev.pomegranate

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.unstabledev.pomegranate.P2PUtils.Observer
import org.unstabledev.pomegranate.P2PUtils.P2PChannelImpl
import org.unstabledev.pomegranate.database.ChatDC

object Repository {
    val fistFilePath = "pomegranate${File.sep}auth.txt"
    val myEmail = File(fistFilePath).readText()

    private val _lastContact = MutableStateFlow<Pair<ChatDC, Observer?>?>(null)
    val lastContact: StateFlow<Pair<ChatDC, Observer?>?> = _lastContact

    val availableChats = mutableMapOf<ChatDC, Observer>()
    var lastOpponentEmail = ""

    fun setLastContact(contact: Pair<ChatDC, Observer?>?) {
        _lastContact.value = contact
    }
}