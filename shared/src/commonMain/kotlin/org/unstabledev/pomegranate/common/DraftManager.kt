package org.unstabledev.pomegranate.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.platform.KMPFile
import org.unstabledev.pomegranate.platform.kmpReadText
import org.unstabledev.pomegranate.platform.kmpWriteText
import kotlin.collections.mutableMapOf

@Serializable
data class MessageDraft(
    val message: String,
)
object DraftManager {
    var drafts = mutableMapOf<Long,MessageDraft>()
    private val FILE_PATH = "${Repository.pomegranatePath}drafts.json"
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun save() {
        try {
            KMPFile(FILE_PATH).kmpWriteText(json.encodeToString(drafts))
            println("Saved drafts")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun load() {
        val file = KMPFile(FILE_PATH)
        try {
            if (!file.exists()) return

            val raw = file.kmpReadText()
            if (raw.isBlank()) return

            drafts = json.decodeFromString<MutableMap<Long,MessageDraft>>(raw)

            println("Loaded drafts")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}