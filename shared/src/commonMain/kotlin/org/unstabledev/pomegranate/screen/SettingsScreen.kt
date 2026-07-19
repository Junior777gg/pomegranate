package org.unstabledev.pomegranate.screen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LensBlur
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.unstabledev.pomegranate.AppSettings
import org.unstabledev.pomegranate.Firebase
import org.unstabledev.pomegranate.FirebaseAddress
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.ThemeMode
import org.unstabledev.pomegranate.applyScreenPadding

@Composable
fun SettingsScreen(navWayObj: NavigationWays) {
    val settings by AppSettings.state.collectAsState()

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
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp)) {
            Text("Внешний вид", fontWeight = FontWeight.SemiBold)
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
                        IconButton(onClick = { AppSettings.setTheme(ThemeMode.SYSTEM) }) {
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
                        IconButton(onClick = { AppSettings.setTheme(ThemeMode.LIGHT) }) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = "Светлая тема",
                                tint = if (settings.theme == ThemeMode.LIGHT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.background,
                            thickness = 3.dp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        IconButton(onClick = { AppSettings.setTheme(ThemeMode.DARK) }) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = "Тёмная тема",
                                tint = if (settings.theme == ThemeMode.DARK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.padding(vertical = 10.dp))
            Text("Сеть", fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(settings.hideSendBarWhenNoNetwork, { AppSettings.setHideSendBarWhenNoNetwork(it) })
                Text("Отключать отправку без интернета")
            }
            Spacer(modifier = Modifier.padding(vertical = 5.dp))
            Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).fillMaxWidth().clickable {
                navWayObj.goTo("select_firebase")
            }) {
                Row(
                    Modifier.background(MaterialTheme.colorScheme.surface).fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Ссылка",
                        tint = if (settings.theme == ThemeMode.DARK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.width(2.dp))
                    Text("Адрес Firebase")
                }
            }
        }
    }
}
