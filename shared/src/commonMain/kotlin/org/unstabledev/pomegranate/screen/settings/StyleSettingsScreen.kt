package org.unstabledev.pomegranate.screen.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.AppSettings
import org.unstabledev.pomegranate.BackgroundStorage
import org.unstabledev.pomegranate.ChatBackgroundIds
import org.unstabledev.pomegranate.platform.MediaSelector
import org.unstabledev.pomegranate.components.HSVColorPicker
import org.unstabledev.pomegranate.components.SettingsPage
import org.unstabledev.pomegranate.components.chat.addChatBackground_defImage
import org.unstabledev.pomegranate.components.chat.addChatBackground_defPrimary
import org.unstabledev.pomegranate.platform.getBitmapFromBytes
import org.unstabledev.pomegranate.platform.kmpCopyTo
import org.unstabledev.pomegranate.platform.kmpReadBytes
import org.unstabledev.pomegranate.screen.nav.NavigationWays

@Composable
fun StyleSettingsScreen(navigationWays: NavigationWays) {
    val settings=AppSettings.state.value
    val scope=rememberCoroutineScope()
    SettingsPage(navigationWays, "Внешний вид") {
        Column {
            Text("Обои", Modifier.padding(bottom = 3.dp))
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    BackgroundSelectButton(
                        ChatBackgroundIds.DEFAULT_PRIMARY,
                        settings.chatBackgroundId
                    )
                    BackgroundSelectButton(
                        ChatBackgroundIds.DEFAULT_IMG01,
                        settings.chatBackgroundId
                    )
                    BackgroundSelectButton(
                        ChatBackgroundIds.DEFAULT_IMG02,
                        settings.chatBackgroundId
                    )
                    BackgroundSelectButton(
                        ChatBackgroundIds.DEFAULT_IMG03,
                        settings.chatBackgroundId
                    )
                    BackgroundSelectButton(
                        ChatBackgroundIds.DEFAULT_IMG04,
                        settings.chatBackgroundId
                    )
                    BackgroundSelectButton(
                        ChatBackgroundIds.DEFAULT_IMG05,
                        settings.chatBackgroundId
                    )
                    BackgroundSelectButton(
                        ChatBackgroundIds.DEFAULT_IMG06,
                        settings.chatBackgroundId
                    )
                    BackgroundSelectButton(
                        ChatBackgroundIds.DEFAULT_IMG07,
                        settings.chatBackgroundId
                    )
                    BackgroundSelectButton(
                        ChatBackgroundIds.DEFAULT_IMG08,
                        settings.chatBackgroundId
                    )

                    val customBgFile = BackgroundStorage.getCustomBackgroundFile()
                    if (customBgFile.exists()) {
                        Box(
                            addChatBackground_defPrimary(
                                Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .then(
                                        if (settings.chatBackgroundId == ChatBackgroundIds.CUSTOM) {
                                            Modifier.border(
                                                3.dp,
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(5.dp)
                                            )
                                        } else Modifier
                                    )
                            ).clickable {
                                AppSettings.setChatBackgroundId(ChatBackgroundIds.CUSTOM)
                            }
                        ) {
                            val bitmap = remember(customBgFile.lastModified()) {
                                getBitmapFromBytes(customBgFile.kmpReadBytes())
                            }
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Custom background",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(Modifier.width(5.dp))
                    }
                    Box(
                        Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .border(
                                3.dp,
                                MaterialTheme.colorScheme.onSurface
                            )
                            .clickable {
                                MediaSelector.Image { image ->
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            val destFile =
                                                BackgroundStorage.getCustomBackgroundFile()
                                            if (destFile.exists()) destFile.delete()
                                            image.kmpCopyTo(destFile)
                                            AppSettings.setChatBackgroundId(
                                                ChatBackgroundIds.CUSTOM
                                            )
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Свои обои",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            if (settings.chatBackgroundId!=ChatBackgroundIds.CUSTOM) {
                Text(
                    when (settings.chatBackgroundId) {
                        ChatBackgroundIds.DEFAULT_IMG08 -> "BatoonTech"
                        else -> "Sanya Alabai"
                    }, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(settings.parseMarkdown, { AppSettings.setParseMarkdown(it) })
            Text("Парсить Markdown")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(settings.chatTripleColumn, { AppSettings.setChatTripleColumn(it) })
            Text("Три линии предпросмотра чата")
        }
        Text("Цвет сообщений")
        HSVColorPicker(Color(settings.messageColor), { AppSettings.setMessageColor(it) })
    }
}

@Composable
private fun BackgroundSelectButton(bgId: Int, currentId: Int) {
    val mod = Modifier
        .size(70.dp)
        .clip(RoundedCornerShape(5.dp))
        .then(
            if (currentId == bgId) {
                Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(5.dp))
            } else Modifier
        )
    Box(
        (if(bgId in 1..15) addChatBackground_defImage(bgId, mod)
        else addChatBackground_defPrimary(mod))
            .clickable { AppSettings.setChatBackgroundId(bgId) }
    ) {}
    Spacer(Modifier.width(5.dp))
}