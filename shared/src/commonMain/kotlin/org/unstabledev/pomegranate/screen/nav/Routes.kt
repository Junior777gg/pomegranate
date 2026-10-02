package org.unstabledev.pomegranate.screen.nav

import kotlinx.serialization.Serializable
import org.unstabledev.pomegranate.database.ChatDC

sealed class Routes {
    class WelcomeScreen: Routes()
    class LoginScreen: Routes()
    class HomeScreen: Routes()
    @Serializable
    data class ChatScreen(val chat : ChatDC): Routes()
    @Serializable
    data class ProfileScreen(val chat : ChatDC): Routes()
    class ContactsScreen: Routes()
    class SettingsScreen: Routes()
    class SettingsStyleScreen: Routes()
    class SettingsNetworkScreen: Routes()
    class SettingsPowerSaveScreen: Routes()
    class SettingsStorageScreen: Routes()
    class SettingsSelectFirebaseScreen: Routes()
    class CallScreen: Routes()
}