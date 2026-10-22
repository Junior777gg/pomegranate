package org.unstabledev.pomegranate.screen.settings

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.common.AppSettings
import org.unstabledev.pomegranate.common.BackgroundStorage
import org.unstabledev.pomegranate.common.ChatBackgroundIds
import org.unstabledev.pomegranate.common.SortingType
import org.unstabledev.pomegranate.common.ThemeMode
import org.unstabledev.pomegranate.components.ColorTheme
import org.unstabledev.pomegranate.platform.MediaSelector
import org.unstabledev.pomegranate.components.HSVColorPicker
import org.unstabledev.pomegranate.components.ScrollablePage
import org.unstabledev.pomegranate.components.SwitchButton
import org.unstabledev.pomegranate.components.chat.MessageBubble
import org.unstabledev.pomegranate.components.chat.addChatBackground
import org.unstabledev.pomegranate.components.chat.addChatBackground_defImage
import org.unstabledev.pomegranate.components.chat.addChatBackground_defPrimary
import org.unstabledev.pomegranate.database.ChatDC
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.platform.HAPTIC_EFFECT_TICK
import org.unstabledev.pomegranate.platform.getBitmapFromBytes
import org.unstabledev.pomegranate.platform.kmpCopyTo
import org.unstabledev.pomegranate.platform.kmpReadBytes
import org.unstabledev.pomegranate.platform.sendHaptic
import kotlin.math.floor
import kotlin.time.Clock.System.now

@Composable
fun StyleSettingsScreen(navController: NavHostController) {
    val settings=AppSettings.state.value
    val scope=rememberCoroutineScope()
    ScrollablePage(navController, "Внешний вид") {
        Column {
            Text("Обои", Modifier.padding(bottom = 3.dp), fontWeight = FontWeight.SemiBold)
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
        SwitchButton(settings.chatTripleColumn, "Три линии предпросмотра чата")
            { AppSettings.setChatTripleColumn(it) }
        Text("Сортировка чатов", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(5.dp))
        Column(Modifier.background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)).padding(all = 3.dp)) {
            Box(Modifier.fillMaxWidth().background(
                if (settings.homeScreenSortingType==SortingType.LAST_UPDATED_DESC) MaterialTheme.colorScheme.background
                else Color.Transparent
            , shape = RoundedCornerShape(9.dp)).padding(horizontal = 7.dp, vertical = 5.dp)
            .clickable { AppSettings.setHomeScreenSortingType(SortingType.LAST_UPDATED_DESC) }) { Text("По последнему изменению") }
            Box(Modifier.fillMaxWidth().background(
                if (settings.homeScreenSortingType==SortingType.LAST_UPDATED_ASC) MaterialTheme.colorScheme.background
                else Color.Transparent
            , shape = RoundedCornerShape(9.dp)).padding(horizontal = 7.dp, vertical = 5.dp)
            .clickable { AppSettings.setHomeScreenSortingType(SortingType.LAST_UPDATED_ASC) }) { Text("По отсутствию изменений") }
            Box(Modifier.fillMaxWidth().background(
                if (settings.homeScreenSortingType==SortingType.LAST_CREATED) MaterialTheme.colorScheme.background
                else Color.Transparent
            , shape = RoundedCornerShape(9.dp)).padding(horizontal = 7.dp, vertical = 5.dp)
            .clickable { AppSettings.setHomeScreenSortingType(SortingType.LAST_CREATED) }) { Text("По дате создания") }
            Box(Modifier.fillMaxWidth().background(
                if (settings.homeScreenSortingType==SortingType.ALPHABETICALLY_ASC) MaterialTheme.colorScheme.background
                else Color.Transparent
            , shape = RoundedCornerShape(9.dp)).padding(horizontal = 7.dp, vertical = 5.dp)
            .clickable { AppSettings.setHomeScreenSortingType(SortingType.ALPHABETICALLY_ASC) }) { Text("По алфавиту (А-Я)") }
            Box(Modifier.fillMaxWidth().background(
                if (settings.homeScreenSortingType==SortingType.ALPHABETICALLY_DESC) MaterialTheme.colorScheme.background
                else Color.Transparent
            , shape = RoundedCornerShape(9.dp)).padding(horizontal = 7.dp, vertical = 5.dp)
            .clickable { AppSettings.setHomeScreenSortingType(SortingType.ALPHABETICALLY_DESC) }) { Text("По алфавиту (Я-А)") }
        }
        Spacer(Modifier.height(5.dp))
        Text("Сообщение", fontWeight = FontWeight.SemiBold)
        Column(addChatBackground(Modifier.fillMaxWidth().padding(10.dp)).clip(RoundedCornerShape(10.dp)).padding(10.dp)) {
            MessageBubble(
                MessageDC(
                    messageCreator = "unknown",
                    data = "Расскажи о себе".encodeToByteArray(),
                    type = MessageDC.TEXT,
                    time = now().toEpochMilliseconds(),
                    isMine = false,
                    chatName = "Судьба",
                    chatType = ChatDC.Companion.ChatTypes.SELFCHAT,
                    chatCreator = Repository.myEmail,
                    supData = byteArrayOf(),
                ),
                setImagePreview = {}, scope = scope, snackBarHostState = SnackbarHostState(), renderMarkdown = settings.parseMarkdown
            )
            Spacer(Modifier.height(5.dp))
            MessageBubble(
                Repository.createMessage(ChatDC("Вы",ChatDC.Companion.ChatTypes.SELFCHAT,
                    Repository.myEmail,mutableListOf(),0),
                    "Я сразу смазал карту будня,\nплеснувши краску из стакана;\nя показал на блюде студня\nкосые скулы океана.\n" +
                            "На чешуе жестяной рыбы\nпрочёл я зовы новых губ.\nА вы\nноктюрн сыграть\nмогли бы\nна флейте водосточных труб?\n\n" +
                            "- Владимир Маяковский", type = MessageDC.TEXT),
                setImagePreview = {}, scope = scope, snackBarHostState = SnackbarHostState(), renderMarkdown = settings.parseMarkdown
            )
        }
        val sliderValue = remember { mutableFloatStateOf(settings.messageRounding) }
        val sliderColors = SliderDefaults.colors().copy(
            activeTickColor = Color.Transparent, inactiveTickColor = Color.Transparent,
            disabledActiveTickColor = Color.Transparent, disabledInactiveTickColor = Color.Transparent
        )
        val sliderState = rememberSliderState(
            value = settings.messageRounding,
            steps = 32,
            valueRange = 0f..32f,
            onValueChangeFinished = {
                AppSettings.setMessageRounding(sliderValue.value)
            }
        )
        LaunchedEffect(sliderState.value) {
            sliderValue.value = sliderState.value
        }
        LaunchedEffect(settings.messageRounding) {
            if (sliderState.value != settings.messageRounding) {
                sliderState.value = settings.messageRounding
                sliderValue.value = settings.messageRounding
            }
        }
        Text("Скругление")
        Slider(sliderState, modifier = Modifier.fillMaxWidth(), colors = sliderColors)

        SwitchButton(settings.parseMarkdown, "Парсить Markdown")
            { AppSettings.setParseMarkdown(it) }
        SwitchButton(settings.messageUnifiedRounding, "Однородное скругление")
            { AppSettings.setMessageUnifiedRounding(it) }
        Text("Цвета", fontWeight = FontWeight.SemiBold)
        val category = remember { mutableStateOf(0) }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1.0f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Row(Modifier.background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)).padding(all = 3.dp)) {
                    IconButton(onClick = { category.value=0 }) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = "Основной",
                            tint = if (category.value==0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    VerticalDivider(
                        color = MaterialTheme.colorScheme.background,
                        thickness = 3.dp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    IconButton(onClick = { category.value=1 }) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "Сообщений",
                            tint = if (category.value==1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
            if (((category.value==0) and (settings.primaryColor!=ColorTheme.AccentDark.toArgb()))
                or ((category.value==1) and (settings.messageColor!=AppSettings.DefaultMessageColor.toArgb()))) {
                IconButton(onClick = {
                    when(category.value) {
                        0->AppSettings.setPrimaryColor(ColorTheme.AccentDark)
                        1->AppSettings.setMessageColor(AppSettings.DefaultMessageColor)
                    }
                }, modifier = Modifier.background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp))) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Восстановить к изначальному",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        when (category.value) {
            0->HSVColorPicker(Color(settings.primaryColor), { AppSettings.setPrimaryColor(it) })
            1->HSVColorPicker(Color(settings.messageColor), { AppSettings.setMessageColor(it) })
        }
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