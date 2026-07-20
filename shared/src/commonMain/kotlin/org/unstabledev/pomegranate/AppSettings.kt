package org.unstabledev.pomegranate

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Clock

@Serializable
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}
@Serializable
data class FirebaseAddress(
    val id: String,
    val title: String,
    val url: String
)

@Serializable
data class AppSettingsState(
    val theme: ThemeMode = ThemeMode.SYSTEM,

    val firebaseAddresses: List<FirebaseAddress> = listOf(
        FirebaseAddress(
            id = "default",
            title = "Гранат",
            url = Secrets.firebaseLink
        )
    ),

    val selectedFirebaseAddressId: String = "default",

    val hideSendBarWhenNoNetwork: Boolean = true,
    val parseMarkdown: Boolean = true,
    val desktopHomeSplit: Float = 1.0f,
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

    private val FILE_PATH = "pomegranate${File.sep}settings.json"
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun save() {
        try {
            File(FILE_PATH).writeText(json.encodeToString(state.value))
            println("Saved app config")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun load() {
        val file = File(FILE_PATH)
        try {
            if (!file.exists()) return

            val raw = file.readText()
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

    fun selectFirebaseAddress(id: String) {
        _state.value = _state.value.copy(selectedFirebaseAddressId = id)
    }

    fun setHideSendBarWhenNoNetwork(v: Boolean) {
        _state.value = _state.value.copy(hideSendBarWhenNoNetwork = v)
    }

    fun setParseMarkdown(v: Boolean) {
        _state.value = _state.value.copy(parseMarkdown = v)
    }

    fun setDesktopHomeSplit(v: Float) {
        _state.value = _state.value.copy(desktopHomeSplit = v)
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