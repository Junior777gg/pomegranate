package org.unstabledev.pomegranate.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.unstabledev.pomegranate.common.altClickable
import org.unstabledev.pomegranate.platform.HAPTIC_EFFECT_CLICK
import org.unstabledev.pomegranate.platform.sendHaptic
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

@Composable
fun Event_NewYearTimer(expanded: MutableState<Boolean>, content: @Composable ColumnScope.() -> Unit) {
    val time=remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while(true) {
            val cur=Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val target = LocalDateTime(
                year = cur.year+1, month = Month(1), day = 1,
                hour = 0, minute = 0, second = 0, nanosecond = 0).toInstant(TimeZone.currentSystemDefault())
            val duration = target-cur.toInstant(TimeZone.currentSystemDefault())
            val days=duration.inWholeDays
            val hours=duration.inWholeHours%24
            val minutes=duration.inWholeMinutes%60
            val seconds=duration.inWholeSeconds%60
            time.value=if(days>0) "$days дн. $hours ч. $minutes мин. $seconds сек."
            else "${hours.toString().padStart(2, '0')}:" +
                    "${minutes.toString().padStart(2, '0')}:" +
                    seconds.toString().padStart(2, '0')
            delay(1.seconds)
        }
    }
    key(time) {
        Column(Modifier.fillMaxWidth().height(70.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)).altClickable({}, {
            expanded.value = true
            sendHaptic(HAPTIC_EFFECT_CLICK)
        }), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("До Нового года осталось", color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp)
            Text(time.value, color = Color.Red, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}