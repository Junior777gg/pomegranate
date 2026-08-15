package org.unstabledev.pomegranate.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.unstabledev.pomegranate.AudioPlayer
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AudioPlayerWidget(
    audioData: String,
    modifier: Modifier = Modifier
) {
    val player = remember { AudioPlayer() }

    val isPrepared = remember { mutableStateOf(false) }
    val isPlaying = remember { mutableStateOf(false) }
    val durationMs = remember { mutableStateOf(0L) }
    val currentPositionMs = remember { mutableStateOf(0L) }
    val loadError = remember { mutableStateOf(false) }
    val sliderColors = SliderColors(
        MaterialTheme.colorScheme.onBackground,
        Color.Gray.copy(alpha = 0.5f),
        Color.Gray.copy(alpha = 0.5f),
        Color.DarkGray.copy(alpha = 0.5f),
        Color.DarkGray.copy(alpha = 0.5f),

        Color.Gray.copy(alpha = 0.5f),
        Color.Gray.copy(alpha = 0.25f),
        Color.Gray.copy(alpha = 0.25f),
        Color.DarkGray.copy(alpha = 0.25f),
        Color.DarkGray.copy(alpha = 0.25f)
    )

    DisposableEffect(Unit) {
        onDispose {
            isPlaying.value = false
            player.release()
        }
    }

    LaunchedEffect(audioData) {
        isPrepared.value = false
        loadError.value = false
        durationMs.value = 0L
        currentPositionMs.value = 0L

        try {
            withContext(Dispatchers.Default) {
                player.release()
                player.setOnCompletionListener {
                    isPlaying.value = false
                    currentPositionMs.value = durationMs.value
                }
                player.setDataSource(audioData)
                player.prepare()
            }
            durationMs.value = player.getDuration()
            isPrepared.value = true
        } catch (_: Exception) {
            loadError.value = true
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (isPrepared.value) {
                if (player.isActive()) {
                    currentPositionMs.value = player.getCurrentPosition().coerceAtLeast(0L)
                    val d = player.getDuration().coerceAtLeast(0L)
                    if (d > 0) durationMs.value = d
                    isPlaying.value = player.isPlaying()
                } else {
                    isPlaying.value = false
                }
            }
            delay(200.milliseconds)
        }
    }

    fun formatTime(ms: Long): String {
        val safe = ms.coerceAtLeast(0L)
        val totalSec = safe / 1000
        val min = totalSec / 60
        val sec = (totalSec % 60).toString().padStart(2, '0')
        return "$min:$sec"
    }

    if (loadError.value) {
        Text(
            text = "Аудио недоступно",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.padding(8.dp)
        )
    } else {
        Row(
            modifier = modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (!isPrepared.value) return@IconButton

                    if (isPlaying.value) {
                        player.pause()
                        isPlaying.value = false
                    } else {
                        if (durationMs.value > 0 && currentPositionMs.value >= durationMs.value) {
                            player.seekTo(0)
                            currentPositionMs.value = 0
                        }
                        player.play()
                        isPlaying.value = true
                    }
                },
                enabled = isPrepared.value
            ) {
                Icon(
                    imageVector = if (isPlaying.value) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                    contentDescription = if (isPlaying.value) "Пауза" else "Воспроизвести",
                    tint = if (isPrepared.value) MaterialTheme.colorScheme.onBackground else Color.Gray.copy(alpha = 0.5f)
                )
            }
            Column(verticalArrangement = Arrangement.Center) {
                Slider(
                    value = currentPositionMs.value.toFloat().coerceIn(
                        0f,
                        if (durationMs.value > 0) durationMs.value.toFloat() else 1f
                    ),
                    onValueChange = { value ->
                        if (isPrepared.value && durationMs.value > 0) {
                            currentPositionMs.value = value.toLong()
                            player.seekTo(value.toLong())
                        }
                    },
                    colors = sliderColors,
                    modifier = Modifier.padding(horizontal = 4.dp).height(20.dp),
                    valueRange = if (durationMs.value > 0) 0f..durationMs.value.toFloat() else 0f..1f,
                    enabled = isPrepared.value && durationMs.value > 0
                )
                if (player.isPlaying()) {
                    Text(
                        modifier = Modifier,
                        text = formatTime(currentPositionMs.value),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    Text(
                        modifier = Modifier,
                        text = formatTime(durationMs.value),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}