package org.unstabledev.pomegranate.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Secrets
import org.unstabledev.pomegranate.platform.Battery
import org.unstabledev.pomegranate.platform.KMPFile
import org.unstabledev.pomegranate.platform.kmpReadText
import org.unstabledev.pomegranate.platform.kmpWriteText
import kotlin.time.Clock

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
    val homeScreenSortingType: Int = SortingType.LAST_UPDATED_DESC,
    val powerSaveSettings: PowerSaveSettings = PowerSaveSettings(),

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

    fun setHomeScreenSortingType(v: Int) {
        _state.value = _state.value.copy(homeScreenSortingType = v)
    }

    fun setKnownSecurityConfig(v: SecurityConfig) {
        _state.value = _state.value.copy(securityConfigKnown = v)
    }

    fun setUnknownSecurityConfig(v: SecurityConfig) {
        _state.value = _state.value.copy(securityConfigUnknown = v)
    }

    fun setPowerSaveSettings(v: PowerSaveSettings) {
        _state.value = _state.value.copy(powerSaveSettings = v)
    }

    fun isInPowerSaveMode(): Boolean =
        ((Battery.isPowerSaveMode()&&_state.value.powerSaveSettings.enableOnPowerSave)
                ||(Battery.getLevel()<=_state.value.powerSaveSettings.percentageTrigger))
                &&!(Battery.isCharging()&&_state.value.powerSaveSettings.accountCharging)

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