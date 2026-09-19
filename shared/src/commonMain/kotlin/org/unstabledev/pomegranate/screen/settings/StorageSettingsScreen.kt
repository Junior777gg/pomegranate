package org.unstabledev.pomegranate.screen.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.platform.KMPFile
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Repository.pomegranatePath
import org.unstabledev.pomegranate.Util
import org.unstabledev.pomegranate.components.SettingsPage
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.platform.formatString
import org.unstabledev.pomegranate.roundTo
import org.unstabledev.pomegranate.screen.nav.NavigationWays
import kotlin.math.max
import kotlin.math.min

@Composable
fun StorageSettingsScreen(navigationWays: NavigationWays, chatDao: ChatDao) {
    val scope=rememberCoroutineScope()
    SettingsPage(navigationWays, "Кэш и хранилище") {
        val deviceTotalVolume = produceState(1L) {
            value = KMPFile(pomegranatePath).getUsableSpace()
        }
        val imagesVolume = produceState(0L) {
            val imgDir = KMPFile("${pomegranatePath}temp/img")
            if (!imgDir.exists()) return@produceState
            val files = imgDir.listFiles()
            if (files?.isEmpty()?:true) return@produceState
            for (file in files) {
                value += file.length()
            }
        }
        val filesVolume = produceState(0L) {
            val imgDir = KMPFile("${pomegranatePath}temp/file")
            if (!imgDir.exists()) return@produceState
            val files = imgDir.listFiles()
            if (files?.isEmpty()?:true) return@produceState
            for (file in files) {
                value += file.length()
            }
        }
        val otherVolume = produceState(0L) {
            val imgDir = KMPFile("${pomegranatePath}temp/dat")
            if (!imgDir.exists()) return@produceState
            val files = imgDir.listFiles()
            if (files?.isEmpty()?:true) return@produceState
            for (file in files) {
                value += file.length()
            }
        }
        val fullVolume = imagesVolume.value + filesVolume.value + otherVolume.value
        fun getArcPercentage(v: Long, full: Long): Float {
            return if (full>0L) min(max((v.toDouble()/full)*360.0,0.0),360.0).toFloat()
            else 0.0f
        }
        Spacer(modifier = Modifier.padding(vertical = 5.dp))
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(Modifier.size(300.dp)) {
                var lastArc = 0.0f

                val imagesArc = getArcPercentage(imagesVolume.value, fullVolume)
                drawArc(Color(1.0f, 0.2f, 0.2f),
                    lastArc, imagesArc, false,
                    style = Stroke(25.dp.toPx(), cap = StrokeCap.Round)
                )
                lastArc += imagesArc
                println(lastArc)

                val filesArc = getArcPercentage(filesVolume.value, fullVolume)
                drawArc(Color(0.8f, 0.3f, 0.8f),
                    lastArc, filesArc, false,
                    style = Stroke(25.dp.toPx(), cap = StrokeCap.Round)
                )
                lastArc += filesArc
                println(lastArc)

                val otherArc = getArcPercentage(otherVolume.value, fullVolume)
                drawArc(Color(0.9f, 0.8f, 0.2f),
                    lastArc, otherArc, false,
                    style = Stroke(25.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        Spacer(modifier = Modifier.padding(vertical = 10.dp))
        val percentagePomer=(fullVolume.toDouble()/deviceTotalVolume.value.toDouble())*100
        var percentagePomerStr=percentagePomer.roundTo(1)
        if (percentagePomer<1) percentagePomerStr=percentagePomer.roundTo(2)
        Text("Pomegranate занимает $percentagePomerStr%",
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.padding(vertical = 5.dp))
        Box(Modifier.clip(RoundedCornerShape(16.dp)).fillMaxWidth()) {
            Column(
                Modifier.background(MaterialTheme.colorScheme.surface).fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Всего: ${Util.formatBinarySize(fullVolume)}")
                Text("Изображения: ${Util.formatBinarySize(imagesVolume.value)}")
                Text("Файлы: ${Util.formatBinarySize(filesVolume.value)}")
                Text("Остальное: ${Util.formatBinarySize(otherVolume.value)}")
            }
        }
        Spacer(modifier = Modifier.padding(vertical = 5.dp))
        Text("Чаты", fontWeight = FontWeight.SemiBold)
        val chatCount = produceState(0) {
            chatDao.getAllChatsFlow().collect { value = it.size }
        }
        Text("Чатов: ${chatCount.value}")
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