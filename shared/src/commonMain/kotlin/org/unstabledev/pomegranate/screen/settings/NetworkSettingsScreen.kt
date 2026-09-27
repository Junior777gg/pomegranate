package org.unstabledev.pomegranate.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.unstabledev.pomegranate.AppSettings
import org.unstabledev.pomegranate.components.SettingsPage
import org.unstabledev.pomegranate.screen.nav.NavigationWays
import org.unstabledev.pomegranate.screen.nav.Routes

@Composable
fun NetworkSettingsScreen(navigationWays: NavigationWays) {
    val settings=AppSettings.state.value
    SettingsPage(navigationWays, "Сеть") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(settings.hideSendBarWhenNoNetwork, { AppSettings.setHideSendBarWhenNoNetwork(it) })
            Text("Отключать отправку без интернета")
        }
        Spacer(modifier = Modifier.padding(vertical = 5.dp))
        Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).fillMaxWidth().clickable {
            navigationWays.goTo(Routes.SETTINGS_SELECT_FIREBASE_SCREEN)
        }) {
            Row(
                Modifier.background(MaterialTheme.colorScheme.surface).fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(2.dp))
                Text("Адрес Firebase")
            }
        }
    }
}