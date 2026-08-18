package org.unstabledev.pomegranate

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.unstabledev.pomegranate.P2PUtils.P2PManagerImpl
import org.unstabledev.pomegranate.Repository.fistFilePath
import org.unstabledev.pomegranate.database.sha256

class BaseP2P {
    init {
        KMPFile("${Repository.pomegranatePath}temp").mkdir()
    }
    val myEmail by lazy { Repository.myEmail}

    companion object {
        val myEmail by lazy { Repository.myEmail.sha256() }
        suspend fun receiveConnections(): Pair<String, P2PManagerImpl> {
            var email = ""
            while (email == "") {
                try {
                    email = Firebase.get<String>("p2p/${myEmail}") ?: ""
                } catch (e: Exception) {
                }
                delay(100)
            }
            println("New connection: $email")
            Firebase.delete("p2p/${myEmail}")
            val manager = P2PManagerImpl("${Repository.pomegranatePath}temp")
            Firebase.put(
                "p2p/${myEmail}/${email.sha256()}/offer",
                "${manager.getAddress()}&${manager.getLocalAddress()}"
            )
            var answer = ""
            while (answer == "") {
                try {
                    answer = Firebase.get<String>("p2p/${myEmail}/${email.sha256()}/answer") ?: ""
                } catch (_: Exception) { }
                delay(100)
            }
            val splitAnswer = answer.split("&")
            manager.createConnection(splitAnswer[0], splitAnswer[1])
            return email to manager
        }
    }

    suspend fun createConnection(email: String): P2PManagerImpl {
        Firebase.delete("p2p/${email.sha256()}")
        try {
            Firebase.put("p2p/${email.sha256()}", myEmail)
        } catch (_: Exception) { }
        var offer = ""
        try {
            withTimeout(5000) {
                while (offer == "") {
                    offer = Firebase.get<String>("p2p/${email.sha256()}/${myEmail.sha256()}/offer") ?: ""
                    delay(100)
                }
            }
        } catch (e: TimeoutCancellationException) {
            throw e
        } finally {
            Firebase.delete("p2p/${email.sha256()}")
        }
        val splitOffer = offer.split("&")
        val manager = P2PManagerImpl("${Repository.pomegranatePath}temp")
        val answer = "${manager.getAddress()}&${manager.getLocalAddress()}"
        try {
            Firebase.put("p2p/${email.sha256()}/${myEmail.sha256()}/answer", answer)
        } catch (_: Exception) { }
        manager.createConnection(splitOffer[0],splitOffer[1])
        return manager
    }
}