package org.unstabledev.pomegranate

import org.unstabledev.pomegranate.P2PUtils.Observer
import org.unstabledev.pomegranate.P2PUtils.P2PChannelImpl
import org.unstabledev.pomegranate.database.ChatDC

object Repository {
    val fistFilePath = "pomegranate${File.sep}firstFile.txt"
    val myEmail = File(fistFilePath).readText()
    var lastContact : Pair<ChatDC, Observer?>? = null
    val availableChats = mutableMapOf<ChatDC, Observer>()

}