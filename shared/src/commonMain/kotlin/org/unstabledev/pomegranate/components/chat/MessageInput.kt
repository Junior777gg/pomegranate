package org.unstabledev.pomegranate.components.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.ReceiveContentListener
import androidx.compose.foundation.content.TransferableContent
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.AudioRecorder
import org.unstabledev.pomegranate.ClipImage
import org.unstabledev.pomegranate.KMPFile
import org.unstabledev.pomegranate.MediaSelector
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Util
import org.unstabledev.pomegranate.Util.Companion.buildTimeMarkMillis
import org.unstabledev.pomegranate.components.ColorTheme
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.handlePastedClipContent
import org.unstabledev.pomegranate.isMobile
import org.unstabledev.pomegranate.screen.control.ChatScreenController
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageInput(
    state: TextFieldState,
    viewModel: ChatScreenController,
    scope: CoroutineScope
) {
    val isAttachMediaOpen = remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    val recorder = remember { AudioRecorder() }
    var currentVoiceFile by remember { mutableStateOf<KMPFile?>(null) }
    val elapsedSeconds = remember { mutableStateOf(0) }
    val pastedClipImages = remember { mutableStateListOf<ClipImage>() }

    LaunchedEffect(Unit) {
        while (true) {
            if (!isRecording) elapsedSeconds.value = 0
            delay(100.milliseconds)
            elapsedSeconds.value++
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            if (recorder.isRecording()) {
                recorder.stop()
            }
            recorder.release()
            pastedClipImages.forEach { it.file?.delete() }
        }
    }

    if (pastedClipImages.isNotEmpty()) {
        pastedClipImages.forEach {
            viewModel.send(files = listOf(it.file!!), type = if (it.mimeType.contains("gif")) MessageDC.ANIMATED_IMAGE else MessageDC.IMAGE)
            pastedClipImages.remove(it)
        }
    }

    Column {
        Row(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0f),
                            Color.Black.copy(alpha = 0.2f)
                        )
                    )
                )
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp, vertical = if (isMobile) {
                        if (Util.isKeyboardVisible()) 48.dp else 25.dp
                    } else 10.dp
                ),
            verticalAlignment = Alignment.Bottom
        ) {
            if (isRecording) {
                Row(
                    Modifier.fillMaxWidth().height(60.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        Modifier.size(50.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(
                        Modifier.size(50.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Row {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(2.dp, CircleShape)
                        .heightIn(min = 40.dp, max = 120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row {
                        AnimatedVisibility(
                            visible = (state.text.isEmpty() && !isRecording),
                            enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it / 2 }),
                            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it / 2 })
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(22.5.dp)
                                    .clickable { isAttachMediaOpen.value = !isAttachMediaOpen.value },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Attachment,
                                    contentDescription = "Файл",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.5.dp).rotate(315.0f)
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                        }

                        if (isRecording) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                var alpha by remember { mutableStateOf(1f) }
                                LaunchedEffect(Unit) {
                                    while (true) {
                                        alpha = if (alpha == 1f) 0.3f else 1f
                                        delay(500.milliseconds)
                                    }
                                }
                                Box(
                                    Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red.copy(alpha = alpha))
                                )
                                Text(
                                    elapsedSeconds.buildTimeMarkMillis(),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp
                                )
                            }
                        } else {
                            var textMod=Modifier.fillMaxWidth()
                            if (isMobile) textMod=textMod.contentReceiver { transferableContent ->
                                handlePastedClipContent(transferableContent, pastedClipImages, scope)
                            }
                            BasicTextField(
                                state = state,
                                modifier = textMod,
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 15.sp
                                ),
                                cursorBrush = SolidColor(ColorTheme.MessageAccent),
                                lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 4),
                                decorator = { innerTextField ->
                                    if (state.text.isEmpty()) {
                                        Text(
                                            text = "Сообщение",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 15.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(ColorTheme.Warning)
                            .clickable {
                                try {
                                    recorder.stop()
                                    isRecording = false

                                    currentVoiceFile?.let { voiceFile ->
                                        voiceFile.delete()
                                        currentVoiceFile = null
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    isRecording = false
                                    currentVoiceFile = null
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Отменить",
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(ColorTheme.MessageAccent)
                        .clickable {
                            val text = state.text.toString().trim()
                            if (isRecording) {
                                try {
                                    recorder.stop()
                                    isRecording = false

                                    currentVoiceFile?.let { voiceFile ->
                                        if (voiceFile.exists() && voiceFile.length() > 0)
                                            scope.launch { viewModel.send(files = listOf(voiceFile), type = MessageDC.FILE) }
                                        else println("Voice file doesn't exist or is empty: ${voiceFile.getAbsolutePath()}")
                                        currentVoiceFile = null
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    isRecording = false
                                    currentVoiceFile = null
                                }
                            } else if (text.isNotEmpty()) {
                                viewModel.send(text, type = MessageDC.TEXT)
                                state.clearText()
                            } else {
                                try {
                                    val tempDir = KMPFile(Repository.pomegranatePath, "temp")
                                    if (!tempDir.exists()) {
                                        tempDir.mkdirs()
                                    }

                                    val voiceFile = KMPFile(
                                        tempDir,
                                        "voice_${Clock.System.now().hashCode()}.ogg"
                                    )

                                    currentVoiceFile = voiceFile
                                    recorder.start(voiceFile)
                                    isRecording = true
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    isRecording = false
                                    currentVoiceFile = null
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording || !state.text.isEmpty()) Icons.Default.Send else Icons.Default.Mic,
                        contentDescription = if (isRecording || !state.text.isEmpty()) "Отправить" else "Записать/Отправить",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (isAttachMediaOpen.value) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable {
                            MediaSelector.MultipleImages { files ->
                                if (files.isNotEmpty()) {
                                    viewModel.send(files = files, type = MessageDC.FILE)
                                }
                                isAttachMediaOpen.value = false
                            }
                        }) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(35.dp)
                        )
                        Text("Изображение", color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable {
                            MediaSelector.MultipleFiles { files ->
                                if (files.isNotEmpty()) {
                                    viewModel.send(files = files, type = MessageDC.FILE)
                                }
                                isAttachMediaOpen.value = false
                            }
                        }) {
                        Icon(
                            imageVector = Icons.Default.FileCopy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(35.dp)
                        )
                        Text("Файл", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}