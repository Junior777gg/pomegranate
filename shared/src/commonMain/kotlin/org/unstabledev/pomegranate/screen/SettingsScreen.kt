package org.unstabledev.pomegranate.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Battery1Bar
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.UsbOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.AppSettings
import org.unstabledev.pomegranate.BackgroundStorage
import org.unstabledev.pomegranate.Battery
import org.unstabledev.pomegranate.ChatBackgroundIds
import org.unstabledev.pomegranate.HAPTIC_EFFECT_TICK
import org.unstabledev.pomegranate.KMPFile
import org.unstabledev.pomegranate.MediaSelector
import org.unstabledev.pomegranate.screen.nav.NavigationWays
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.screen.nav.Routes
import org.unstabledev.pomegranate.ThemeMode
import org.unstabledev.pomegranate.Util
import org.unstabledev.pomegranate.components.HSVColorPicker
import org.unstabledev.pomegranate.screen.nav.applyScreenPadding
import org.unstabledev.pomegranate.components.chat.addChatBackground_defImage
import org.unstabledev.pomegranate.components.chat.addChatBackground_defPrimary
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.getBitmapFromBytes
import org.unstabledev.pomegranate.isMobile
import org.unstabledev.pomegranate.kmpCopyTo
import org.unstabledev.pomegranate.kmpReadBytes
import org.unstabledev.pomegranate.sendHaptic
import kotlin.math.floor
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SettingsScreen(navWayObj: NavigationWays, chatDao: ChatDao) {
    val settings by AppSettings.state.collectAsState()
    val scope = rememberCoroutineScope()
    val amoledClicks = remember { mutableStateOf(0) }

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
                    Column {
                        if (settings.amoledUnlocked && settings.theme==ThemeMode.SYSTEM) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(settings.useAmoledOnDarkSystem, { AppSettings.setUseAmoledOnDarkSystem(it) })
                                Text("Amoled вместо Тёмной темы")
                            }
                        }
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
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))
                    Text("Сеть", fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(settings.hideSendBarWhenNoNetwork, { AppSettings.setHideSendBarWhenNoNetwork(it) })
                        Text("Отключать отправку без интернета")
                    }
                    Spacer(modifier = Modifier.padding(vertical = 5.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).fillMaxWidth().clickable {
                        navWayObj.goTo(Routes.SETTINGS_SELECT_FIREBASE_SCREEN)
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
                    if (isMobile) {
                        Spacer(modifier = Modifier.padding(vertical = 10.dp))
                        Row {
                            Text("Энергосбережение", fontWeight = FontWeight.SemiBold)
                            AnimatedVisibility(AppSettings.isInPowerSaveMode()) {
                                Text(" Активно", color = MaterialTheme.colorScheme.primary)
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
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))
                    Text("Хранилище и кэш", fontWeight = FontWeight.SemiBold)
                    val chatCount = produceState(0) { chatDao.getAllChatsFlow().collect {
                        value = it.size
                    } }
                    Text("Чатов: ${chatCount.value}")
                    val chatCacheSize = remember { KMPFile("${Repository.pomegranatePath}chat.db").length()}
                    Text("Размер БД чатов: ${Util.formatBinarySize(chatCacheSize)}")
                    val chatMsgCacheSize = remember { KMPFile("${Repository.pomegranatePath}messages.db").length() }
                    Text("Размер БД сообщений: ${Util.formatBinarySize(chatMsgCacheSize)}")
                    Spacer(modifier = Modifier.padding(vertical = 5.dp))
                    var showDeleteChatsPopup by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).fillMaxWidth().clickable {
                        showDeleteChatsPopup = true
                    }) {
                        Row(
                            Modifier.background(MaterialTheme.colorScheme.surface).fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(2.dp))
                            Text("Удалить все чаты", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    if (showDeleteChatsPopup) {
                        val onDismiss = {showDeleteChatsPopup = false}
                        AlertDialog(
                            onDismissRequest = onDismiss,
                            title = {
                                Text("Вы уверены что хотите удалить все чаты?", color = MaterialTheme.colorScheme.onBackground)
                            },
                            text = {
                                Text("Это действие безвозвратно!")
                            },
                            confirmButton = {
                                Text("Подтвердить", Modifier.clickable {
                                    scope.launch { chatDao.deleteAllChats() }
                                    showDeleteChatsPopup = false
                                })
                            },
                            dismissButton = {
                                Text("Отмена", Modifier.clickable {
                                    showDeleteChatsPopup = false
                                })
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BackgroundSelectButton(bgId: Int, currentId: Int) {
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
