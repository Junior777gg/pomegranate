package org.unstabledev.pomegranate.screen.nav

import kotlinx.serialization.Serializable

sealed class Routes {
    @Serializable
    class Onboarding: Routes()
    @Serializable
    class SwitchAccount: Routes()
    @Serializable
    class Home: Routes()
    @Serializable
    data class Chat(val chatId : Long): Routes()
    @Serializable
    data class Profile(val chatId : Long?, val email: String?): Routes()
    @Serializable
    class Contacts: Routes()
    @Serializable
    class Settings: Routes()
    @Serializable
    class SettingsStyle: Routes()
    @Serializable
    class SettingsNetwork: Routes()
    @Serializable
    class SettingsPowerSave: Routes()
    @Serializable
    class SettingsStorage: Routes()
    @Serializable
    class SettingsDeveloper: Routes()
    @Serializable
    class SettingsSelectFirebase: Routes()
    @Serializable
    class Call: Routes()
    @Serializable
    data class ImagePreview(val messageId : Long): Routes()
}