package org.unstabledev.pomegranate.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery1Bar
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.UsbOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.unstabledev.pomegranate.AppSettings
import org.unstabledev.pomegranate.platform.HAPTIC_EFFECT_TICK
import org.unstabledev.pomegranate.components.SettingsPage
import org.unstabledev.pomegranate.screen.nav.NavigationWays
import org.unstabledev.pomegranate.platform.sendHaptic
import kotlin.math.floor

@Composable
fun PowerSaveSettingsScreen(navigationWays: NavigationWays) {
    val settings=AppSettings.state.value
    SettingsPage(navigationWays, "Энергосбережение") {
        Row {
            AnimatedVisibility(!AppSettings.isInPowerSaveMode()) {
                Text("Неактивно", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
            AnimatedVisibility(AppSettings.isInPowerSaveMode()) {
                Text("Активно", color = MaterialTheme.colorScheme.primary)
            }
        }

        val sliderColors = SliderDefaults.colors().copy(
            activeTickColor = Color.Transparent, inactiveTickColor = Color.Transparent,
            disabledActiveTickColor = Color.Transparent, disabledInactiveTickColor = Color.Transparent
        )

        val currentPowerSave by rememberUpdatedState(settings.powerSaveSettings)

        val sliderValue = remember { mutableFloatStateOf(settings.powerSaveSettings.percentageTrigger) }

        val sliderState = rememberSliderState(
            value = settings.powerSaveSettings.percentageTrigger,
            steps = 99,
            valueRange = 0f..100f,
            onValueChangeFinished = {
                AppSettings.setPowerSaveSettings(
                    currentPowerSave.copy(percentageTrigger = sliderValue.value)
                )
            }
        )

        LaunchedEffect(sliderState.value) {
            sliderValue.value = sliderState.value
        }

        LaunchedEffect(settings.powerSaveSettings.percentageTrigger) {
            if (sliderState.value != settings.powerSaveSettings.percentageTrigger) {
                sliderState.value = settings.powerSaveSettings.percentageTrigger
                sliderValue.value = settings.powerSaveSettings.percentageTrigger
            }
        }

        val triggerCharge = floor(sliderState.value).toInt()

        Slider(sliderState, modifier = Modifier.fillMaxWidth(), colors = sliderColors)
        Text(
            when (triggerCharge) {
                100 -> "Всегда включено"
                0 -> "Всегда выключено"
                else -> "Включать при заряде менее: $triggerCharge%"
            }
        )

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(32.dp)).height(64.dp)
        ) {
            Box(
                Modifier.background(MaterialTheme.colorScheme.surface).fillMaxSize()
            ) {
                Row(
                    Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = {
                        AppSettings.setPowerSaveSettings(
                            settings.powerSaveSettings.copy(
                                accountCharging = !settings.powerSaveSettings.accountCharging
                            )
                        )
                        sendHaptic(HAPTIC_EFFECT_TICK)
                    }) {
                        Icon(
                            imageVector = if (settings.powerSaveSettings.accountCharging) Icons.Default.Usb else Icons.Default.UsbOff,
                            contentDescription = "Учитывать батарею",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    VerticalDivider(
                        color = MaterialTheme.colorScheme.background,
                        thickness = 3.dp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    IconButton(onClick = {
                        AppSettings.setPowerSaveSettings(
                            settings.powerSaveSettings.copy(
                                enableOnPowerSave = !settings.powerSaveSettings.enableOnPowerSave
                            )
                        )
                        sendHaptic(HAPTIC_EFFECT_TICK)
                    }) {
                        Icon(
                            imageVector = if (settings.powerSaveSettings.enableOnPowerSave) Icons.Default.Battery1Bar else Icons.Default.BatteryAlert,
                            contentDescription = "Учитывать системный режим энергосбережения",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(settings.powerSaveSettings.animateGifs, {
                AppSettings.setPowerSaveSettings(settings.powerSaveSettings.copy(animateGifs = it))
            })
            Text("Анимировать изображения")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(settings.powerSaveSettings.decodeImages, {
                AppSettings.setPowerSaveSettings(settings.powerSaveSettings.copy(decodeImages = it))
            })
            Text("Декодировать изображения")
        }
    }
}