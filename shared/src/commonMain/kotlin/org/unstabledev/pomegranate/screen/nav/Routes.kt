package org.unstabledev.pomegranate.screen.nav

import kotlinx.serialization.Serializable

sealed class Routes {
    @Serializable
    class WelcomeScreen: Routes()
    @Serializable
    class LoginScreen: Routes()
    @Serializable
    class HomeScreen: Routes()
    @Serializable
    data class ChatScreen(val chatId : Long): Routes()
    @Serializable
    data class ProfileScreen(val chatId : Long): Routes()
    @Serializable
    class ContactsScreen: Routes()
    @Serializable
    class SettingsScreen: Routes()
    @Serializable
    class SettingsStyleScreen: Routes()
    @Serializable
    class SettingsNetworkScreen: Routes()
    @Serializable
    class SettingsPowerSaveScreen: Routes()
    @Serializable
    class SettingsStorageScreen: Routes()
    @Serializable
    class SettingsSelectFirebaseScreen: Routes()
    @Serializable
    class CallScreen: Routes()
    @Serializable
    data class ImagePreview(val messageId : Long): Routes()
}