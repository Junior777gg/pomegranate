package org.unstabledev.pomegranate

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.fleeksoft.charset.toByteArray
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Clock

@Serializable
data class SecurityConfig(
    var allowText: Boolean = true,
    var allowImages: Boolean = true,
    var allowAudio: Boolean = true,
    var allowFiles: Boolean = true,
    var allowCalls: Boolean = true,
) {
    override fun toString(): String {
        var out=""
        out += if(allowText) "1" else "0"
        out += if(allowImages) "1" else "0"
        out += if(allowAudio) "1" else "0"
        out += if(allowFiles) "1" else "0"
        out += if(allowCalls) "1" else "0"
        return out
    }
    fun fromString(str: String) {
        if (str.length!=5) return
        fun b(str: Char) = str=='1'
        allowText=b(str[0])
        allowImages=b(str[1])
        allowAudio=b(str[2])
        allowFiles=b(str[3])
        allowCalls=b(str[4])
    }
    fun toByteArray(): ByteArray {
        return this.toString().toByteArray()
    }
    fun fromByteArray(bytes: ByteArray) {
        fromString(bytes.decodeToString())
    }

    companion object {
    }
}
@Serializable
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED
}
@Serializable
data class FirebaseAddress(
    val id: String,
    val title: String,
    val url: String
)

object ChatBackgroundIds {
    const val DEFAULT_PRIMARY = 0
    const val DEFAULT_IMG01 = 1
    const val DEFAULT_IMG02 = 2
    const val DEFAULT_IMG03 = 3
    const val DEFAULT_IMG04 = 4
    const val DEFAULT_IMG05 = 5
    const val DEFAULT_IMG06 = 6
    const val DEFAULT_IMG07 = 7
    const val DEFAULT_IMG08 = 8
    const val CUSTOM = 16
}

object BackgroundStorage {
    val CUSTOM_BACKGROUND_PATH = "${Repository.pomegranatePath}backgrounds/"

    fun ensureBackgroundDir() {
        KMPFile(CUSTOM_BACKGROUND_PATH).mkdirs()
    }

    fun getCustomBackgroundFile(): KMPFile {
        ensureBackgroundDir()
        return KMPFile(CUSTOM_BACKGROUND_PATH, "custom_bg.jpg")
    }
}

@Serializable
data class AppSettingsState(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val chatBackgroundId: Int = ChatBackgroundIds.DEFAULT_PRIMARY,
    val messageColor: Int = 0x8BFF1A,
    val hideSendBarWhenNoNetwork: Boolean = true,
    val parseMarkdown: Boolean = true,
    val chatTripleColumn: Boolean = false,
    val amoledUnlocked: Boolean = false,
    val useAmoledOnDarkSystem: Boolean = false,
    val desktopHomeSplit: Float = 1.0f,

    val firebaseAddresses: List<FirebaseAddress> = listOf(
        FirebaseAddress(
            id = "default",
            title = "Гранат",
            url = Secrets.firebaseLink
        )
    ),
    val selectedFirebaseAddressId: String = "default",

    val securityConfigKnown: SecurityConfig = SecurityConfig(),
    val securityConfigUnknown: SecurityConfig = SecurityConfig(),
) {
    val selectedFirebaseUrl: String
        get() = firebaseAddresses
            .firstOrNull { it.id == selectedFirebaseAddressId }
            ?.url
            ?: Secrets.firebaseLink
}

object AppSettings {
    private val _state = MutableStateFlow(AppSettingsState())
    val state: StateFlow<AppSettingsState> = _state

    private val FILE_PATH = "${Repository.pomegranatePath}settings.json"
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun save() {
        try {
            KMPFile(FILE_PATH).kmpWriteText(json.encodeToString(state.value))
            println("Saved app config")
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

            val loaded = json.decodeFromString<AppSettingsState>(raw)
            val ensured = ensureDefaultFirebase(loaded)

            _state.value = ensured

            println("Loaded app config")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun ensureDefaultFirebase(
        loaded: AppSettingsState
    ): AppSettingsState {
        val hasDefault = loaded.firebaseAddresses.any {
            it.id == "default"
        }

        if (!hasDefault) {
            val defaultAddr = FirebaseAddress(
                id = "default",
                title = "Гранат",
                url = Secrets.firebaseLink
            )

            return loaded.copy(
                firebaseAddresses = listOf(defaultAddr) +
                        loaded.firebaseAddresses
            )
        }

        return loaded
    }

    fun setTheme(mode: ThemeMode) {
        _state.value = _state.value.copy(theme = mode)
    }

    fun setChatBackgroundId(id: Int) {
        _state.value = _state.value.copy(chatBackgroundId = id)
    }

    fun selectFirebaseAddress(id: String) {
        _state.value = _state.value.copy(selectedFirebaseAddressId = id)
    }

    fun setHideSendBarWhenNoNetwork(v: Boolean) {
        _state.value = _state.value.copy(hideSendBarWhenNoNetwork = v)
    }

    fun setUseAmoledOnDarkSystem(v: Boolean) {
        _state.value = _state.value.copy(useAmoledOnDarkSystem = v)
    }

    fun setAmoledUnlocked(v: Boolean) {
        _state.value = _state.value.copy(amoledUnlocked = v)
    }

    fun setParseMarkdown(v: Boolean) {
        _state.value = _state.value.copy(parseMarkdown = v)
    }

    fun setChatTripleColumn(v: Boolean) {
        _state.value = _state.value.copy(chatTripleColumn = v)
    }

    fun setDesktopHomeSplit(v: Float) {
        _state.value = _state.value.copy(desktopHomeSplit = v)
    }

    fun setMessageColor(v: Color) {
        _state.value = _state.value.copy(messageColor = v.toArgb())
    }

    fun setKnownSecurityConfig(v: SecurityConfig) {
        _state.value = _state.value.copy(securityConfigKnown = v)
    }

    fun setUnknownSecurityConfig(v: SecurityConfig) {
        _state.value = _state.value.copy(securityConfigKnown = v)
    }

    fun addFirebaseAddress(title: String, url: String) {
        val cleanUrl = normalizeFirebaseUrl(url)

        if (cleanUrl.isBlank()) return

        val address = FirebaseAddress(
            id = "firebase_${Clock.System.now().hashCode()}",
            title = title.ifBlank { cleanUrl },
            url = cleanUrl
        )

        _state.value = _state.value.copy(
            firebaseAddresses = _state.value.firebaseAddresses + address,
            selectedFirebaseAddressId = address.id
        )
    }

    fun updateFirebaseAddress(
        id: String,
        title: String,
        url: String
    ) {
        val cleanUrl = normalizeFirebaseUrl(url)

        if (cleanUrl.isBlank()) return

        val currentState = _state.value

        _state.value = currentState.copy(
            firebaseAddresses = currentState.firebaseAddresses.map { address ->
                if (address.id == id) {
                    address.copy(
                        title = title.ifBlank { cleanUrl },
                        url = cleanUrl
                    )
                } else {
                    address
                }
            }
        )
    }

    fun removeFirebaseAddress(id: String) {
        if (id == "default") return

        val newList = _state.value.firebaseAddresses.filterNot { it.id == id }

        val newSelectedId =
            if (_state.value.selectedFirebaseAddressId == id) {
                newList.firstOrNull()?.id ?: "default"
            } else {
                _state.value.selectedFirebaseAddressId
            }

        _state.value = _state.value.copy(
            firebaseAddresses = newList,
            selectedFirebaseAddressId = newSelectedId
        )
    }

    private fun normalizeFirebaseUrl(url: String): String {
        return url
            .trim()
            .removeSuffix(".json")
            .trimEnd('/')
    }

    @Composable
    fun isLightTheme(settings: AppSettingsState): Boolean {
        return settings.theme==ThemeMode.LIGHT||(settings.theme==ThemeMode.SYSTEM&&!isSystemInDarkTheme())
    }
}