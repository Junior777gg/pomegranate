package org.unstabledev.pomegranate.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.unstabledev.pomegranate.api.event.PublicEventHandler
import org.unstabledev.pomegranate.api.event.StandardPublicEvents
import org.unstabledev.pomegranate.common.AppSettings
import org.unstabledev.pomegranate.common.AppTracer
import org.unstabledev.pomegranate.common.Util.Companion.toDatedHHMMTime
import org.unstabledev.pomegranate.common.collectAppSettings
import org.unstabledev.pomegranate.components.ScrollablePage
import org.unstabledev.pomegranate.components.SwitchButton

@Composable
fun DeveloperSettingsScreen(navController: NavHostController) {
    val settings=collectAppSettings()
    ScrollablePage(navController, "Для разработчиков") {
        Text("Поведение приложения", fontWeight = FontWeight.SemiBold)
        SwitchButton(settings.executeBackgroundService, "Запускать фоновый сервис") {
            AppSettings.setExecuteBackgroundService(it)
        }
        Text("Публичные события", fontWeight = FontWeight.SemiBold)
        val updatedEvent=mutableStateOf(false)
        key(updatedEvent.value) {
            Column {
                for (event in StandardPublicEvents.LIST_ALL) {
                    Row(Modifier.fillMaxWidth().padding(3.dp).clickable {
                        PublicEventHandler.currentEvent=event
                        updatedEvent.value=!updatedEvent.value
                    }) {
                        if (PublicEventHandler.currentEvent.id==event.id) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = event.bgColor
                            )
                            Spacer(Modifier.width(3.dp))
                        }
                        Text(event.title)
                        Text(" [${event.id}]", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                }
            }
        }
        Button({
            AppSettings.setLastHiddenEventId("")
        }) {
            Text("Сбросить скрытое событие")
        }
        Text("События P2P", fontWeight = FontWeight.SemiBold)
        key(AppTracer.isRunning()) {
            Button({
                if (AppTracer.isRunning()) AppTracer.stop() else AppTracer.start()
            }) {
                Text(if (AppTracer.isRunning()) "Завершить трассировку" else "Начать трассировку")
            }
        }
        key(AppTracer.dump()) {
            for (trace in AppTracer.dump()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(trace.origin, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(3.dp))
                    Text(trace.time.toDatedHHMMTime())
                }
            }
        }
    }
}