package org.unstabledev.pomegranate.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.unstabledev.pomegranate.AppSettings
import org.unstabledev.pomegranate.platform.HAPTIC_EFFECT_TICK
import org.unstabledev.pomegranate.screen.nav.NavigationWays
import org.unstabledev.pomegranate.screen.nav.Routes
import org.unstabledev.pomegranate.ThemeMode
import org.unstabledev.pomegranate.screen.nav.applyScreenPadding
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.platform.isMobile
import org.unstabledev.pomegranate.platform.sendHaptic

@Composable
fun SettingsScreen(navWayObj: NavigationWays, chatDao: ChatDao) {
    val settings by AppSettings.state.collectAsState()
    val scope = rememberCoroutineScope()

    Column(applyScreenPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                navWayObj.back()
                AppSettings.save()
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "Настройки",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        LazyColumn {
            item {
                val amoledClicks = remember { mutableStateOf(0) }
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
                                AppSettings.setTheme(ThemeMode.SYSTEM)
                                sendHaptic(HAPTIC_EFFECT_TICK)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.BrightnessAuto,
                                    contentDescription = "Системная тема",
                                    tint = if (settings.theme == ThemeMode.SYSTEM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                )
                            }
                            VerticalDivider(
                                color = MaterialTheme.colorScheme.background,
                                thickness = 3.dp,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                            IconButton(onClick = {
                                AppSettings.setTheme(ThemeMode.LIGHT)
                                sendHaptic(HAPTIC_EFFECT_TICK)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.LightMode,
                                    contentDescription = "Светлая",
                                    tint = if (settings.theme == ThemeMode.LIGHT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                )
                            }
                            VerticalDivider(
                                color = MaterialTheme.colorScheme.background,
                                thickness = 3.dp,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                            IconButton(onClick = {
                                AppSettings.setTheme(ThemeMode.DARK)
                                sendHaptic(HAPTIC_EFFECT_TICK)
                                amoledClicks.value += 1
                                if (amoledClicks.value>3) AppSettings.setAmoledUnlocked(true)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = "Тёмная",
                                    tint = if (settings.theme == ThemeMode.DARK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                )
                            }
                            if (settings.amoledUnlocked) {
                                VerticalDivider(
                                    color = MaterialTheme.colorScheme.background,
                                    thickness = 3.dp,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                                IconButton(onClick = {
                                    AppSettings.setTheme(ThemeMode.AMOLED)
                                    sendHaptic(HAPTIC_EFFECT_TICK)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ShieldMoon,
                                        contentDescription = "Amoled",
                                        tint = if (settings.theme == ThemeMode.AMOLED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
                if (settings.amoledUnlocked && settings.theme==ThemeMode.SYSTEM) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(settings.useAmoledOnDarkSystem, { AppSettings.setUseAmoledOnDarkSystem(it) })
                        Text("Amoled вместо Тёмной темы")
                    }
                }
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp)) {
                    SettingsPageButton(Color(0.9f, 0.6f, 0.2f), Icons.Default.Palette, "Внешний вид")
                        { navWayObj.goTo(Routes.SETTINGS_STYLE_SCREEN) }
                    Spacer(Modifier.height(8.dp))
                    SettingsPageButton(Color(0.2f, 0.6f, 0.9f), Icons.Default.NetworkWifi, "Сеть")
                        { navWayObj.goTo(Routes.SETTINGS_NETWORK_SCREEN) }
                    if (isMobile) {
                        Spacer(Modifier.height(8.dp))
                        SettingsPageButton(Color(0.2f, 0.8f, 0.4f), Icons.Default.BatterySaver, "Энергосбережение")
                            { navWayObj.goTo(Routes.SETTINGS_POWER_SAVE_SCREEN) }
                    }
                    Spacer(Modifier.height(8.dp))
                    SettingsPageButton(Color(0.7f, 0.3f, 0.9f), Icons.Default.Storage, "Кэш и хранилище")
                        { navWayObj.goTo(Routes.SETTINGS_STORAGE_SCREEN) }
                }
            }
        }
    }
}

@Composable
fun SettingsPageButton(color: Color, icon: ImageVector, header: String, onClick: ()->Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .background(color, RoundedCornerShape(8.dp))
                .size(30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface
            )
        }
        VerticalDivider(
            color = Color.Transparent,
            thickness = 3.dp,
            modifier = Modifier
                .padding(start = 16.dp)
                .height(30.dp)
        )
        Text(
            text = header,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
