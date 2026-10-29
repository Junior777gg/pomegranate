package org.unstabledev.pomegranate.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.unstabledev.pomegranate.common.AppSettings
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun MuteChatAlert(showMuteChatPopup: MutableState<Boolean>, chatKey: Long) {
    AlertDialog(
        onDismissRequest = { showMuteChatPopup.value = false },
        title = {
            Text(
                "Выключить уведомления на...",
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().clickable {
                    AppSettings.muteChat(chatKey, Long.MAX_VALUE)
                    showMuteChatPopup.value = false
                }.padding(5.dp), horizontalArrangement = Arrangement.Center) { Text("всегда") }

                Row(Modifier.fillMaxWidth().clickable {
                    val target=Clock.System.now()+30.minutes
                    AppSettings.muteChat(chatKey, target.toEpochMilliseconds())
                    showMuteChatPopup.value = false
                }.padding(5.dp), horizontalArrangement = Arrangement.Center) { Text("30 мин.") }

                Row(Modifier.fillMaxWidth().clickable {
                    val target=Clock.System.now()+1.hours
                    AppSettings.muteChat(chatKey, target.toEpochMilliseconds())
                    showMuteChatPopup.value = false
                }.padding(5.dp), horizontalArrangement = Arrangement.Center) { Text("1 час") }

                Row(Modifier.fillMaxWidth().clickable {
                    val target=Clock.System.now()+2.hours
                    AppSettings.muteChat(chatKey, target.toEpochMilliseconds())
                    showMuteChatPopup.value = false
                }.padding(5.dp), horizontalArrangement = Arrangement.Center) { Text("2 час") }

                Row(Modifier.fillMaxWidth().clickable {
                    val target=Clock.System.now()+8.hours
                    AppSettings.muteChat(chatKey, target.toEpochMilliseconds())
                    showMuteChatPopup.value = false
                }.padding(5.dp), horizontalArrangement = Arrangement.Center) { Text("8 час") }

                Row(Modifier.fillMaxWidth().clickable {
                    val target=Clock.System.now()+1.days
                    AppSettings.muteChat(chatKey, target.toEpochMilliseconds())
                    showMuteChatPopup.value = false
                }.padding(5.dp), horizontalArrangement = Arrangement.Center) { Text("1 день") }

                Row(Modifier.fillMaxWidth().clickable {
                    val target=Clock.System.now()+2.days
                    AppSettings.muteChat(chatKey, target.toEpochMilliseconds())
                    showMuteChatPopup.value = false
                }.padding(5.dp), horizontalArrangement = Arrangement.Center) { Text("2 дня") }

                Row(Modifier.fillMaxWidth().clickable {
                    val target=Clock.System.now()+7.days
                    AppSettings.muteChat(chatKey, target.toEpochMilliseconds())
                    showMuteChatPopup.value = false
                }.padding(5.dp), horizontalArrangement = Arrangement.Center) { Text("1 нед.") }

                Row(Modifier.fillMaxWidth().clickable {
                    val target=Clock.System.now()+14.days
                    AppSettings.muteChat(chatKey, target.toEpochMilliseconds())
                    showMuteChatPopup.value = false
                }.padding(5.dp), horizontalArrangement = Arrangement.Center) { Text("2 нед.") }

                Row(Modifier.fillMaxWidth().clickable {
                    val target=Clock.System.now()+31.days
                    AppSettings.muteChat(chatKey, target.toEpochMilliseconds())
                    showMuteChatPopup.value = false
                }.padding(5.dp), horizontalArrangement = Arrangement.Center) { Text("1 мес.") }

                Row(Modifier.fillMaxWidth().clickable {
                    val target=Clock.System.now()+62.days
                    AppSettings.muteChat(chatKey, target.toEpochMilliseconds())
                    showMuteChatPopup.value = false
                }.padding(5.dp), horizontalArrangement = Arrangement.Center) { Text("2 мес.") }

                Row(Modifier.fillMaxWidth().clickable {
                    val target=Clock.System.now()+365.days
                    AppSettings.muteChat(chatKey, target.toEpochMilliseconds())
                    showMuteChatPopup.value = false
                }.padding(5.dp), horizontalArrangement = Arrangement.Center) { Text("1 год") }
            }
        },
        confirmButton = {
        },
        dismissButton = {
        }
    )
}