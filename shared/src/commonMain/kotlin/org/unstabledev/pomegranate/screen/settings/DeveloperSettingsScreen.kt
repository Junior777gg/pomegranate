package org.unstabledev.pomegranate.screen.settings

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import org.unstabledev.pomegranate.api.event.PublicEventHandler
import org.unstabledev.pomegranate.common.AppSettings
import org.unstabledev.pomegranate.components.ScrollablePage

@Composable
fun DeveloperSettingsScreen(navController: NavHostController) {
    val settings=AppSettings.state.value
    ScrollablePage(navController, "Для разработчиков") {
        Text("Публичные события")
        Button({
            PublicEventHandler.eventId="test"
            PublicEventHandler.title="Test"
            PublicEventHandler.description="This is an event"
        }) {
            Text("Тестовое событие")
        }
        Button({
            AppSettings.setLastHiddenEventId("")
        }) {
            Text("Сбросить скрытое событие")
        }
    }
}