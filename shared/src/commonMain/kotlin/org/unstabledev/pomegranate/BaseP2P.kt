package org.unstabledev.pomegranate

import kotlinx.coroutines.delay
import org.unstabledev.pomegranate.P2PUtils.P2PChannelImpl
import org.unstabledev.pomegranate.P2PUtils.P2PManagerImpl
import org.unstabledev.pomegranate.database.decodeFromSha256
import org.unstabledev.pomegranate.database.sha256

class BaseP2P {
    val myEmail = File(Repository.fistFilePath).readText()

    companion object {
        val myEmail = File(Repository.fistFilePath).readText().sha256()
        suspend fun receiveConnections(): Pair<String, P2PChannelImpl> {
            var email = ""
            while (email == "") {
                try {
                email = Firebase.get<String>("p2p/${myEmail}") ?:""
                } catch (e: Exception) {}
                delay(100)
            }
            println("new connections: $email")
            Firebase.delete("p2p/${myEmail}")
            val manager = P2PManagerImpl()
            Firebase.put(
                "p2p/${myEmail}/${email.sha256()}/offer",
                "${manager.getAddress()}&${manager.getLocalAddress()}&${manager.getPublicKeyJson()}"
            )
            var answer = ""
            while (answer == "") {
                try {
                answer = Firebase.get<String>("p2p/${myEmail}/${email.sha256()}/answer")?:""
                } catch (e: Exception) {}
                delay(100)
            }
            val splitAnswer = answer.split("&")
            val channel = manager.createConnection(splitAnswer[0], splitAnswer[1], splitAnswer[2])
            return email to channel
        }
    }

    suspend fun createConnection(email: String): P2PChannelImpl {
        try {
        Firebase.put("p2p/${email.sha256()}", myEmail)
        } catch (e: Exception) {}
        var offer = ""
        while (offer == "") {
            offer = Firebase.get<String>("p2p/${email.sha256()}/${myEmail.sha256()}/offer")?:""
            delay(100)
        }
        val splitOffer = offer.split("&")
        val manager = P2PManagerImpl()
        val answer = "${manager.getAddress()}&${manager.getLocalAddress()}&${manager.getPublicKeyJson()}"
        try {
        Firebase.put("p2p/${email.sha256()}/${myEmail.sha256()}/answer", answer)}catch (e: Exception) {}
        return manager.createConnection(splitOffer[0], splitOffer[1], splitOffer[2])
    }
}