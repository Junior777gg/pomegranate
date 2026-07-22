package org.unstabledev.pomegranate.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.FileSaver
import org.unstabledev.pomegranate.applyScreenPadding
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.getBitmapFromBytes
import kotlin.time.Clock

@Composable
fun ImagePreviewPanel(onBack: ()->Unit, message: MessageDC?, snackbarHostState: SnackbarHostState) {
    if (message == null) {
        onBack()
        return
    }

    val menuExpanded = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bitmap = getBitmapFromBytes(message.data)

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    fun maxOffsetForScale(s: Float): Offset {
        val bw = bitmap.width.toFloat()
        val bh = bitmap.height.toFloat()
        val cw = containerSize.width.toFloat()
        val ch = containerSize.height.toFloat()
        if (cw <= 0f || ch <= 0f) return Offset.Zero

        val fitScale = minOf(cw / bw, ch / bh)
        val renderedW = bw * fitScale * s
        val renderedH = bh * fitScale * s

        return Offset(
            maxOf(0f, (renderedW - cw) / 2f),
            maxOf(0f, (renderedH - ch) / 2f)
        )
    }

    Box(
        modifier = applyScreenPadding()
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { containerSize = it }
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures(panZoomLock = true) { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(1f, 5f)
                        val max = maxOffsetForScale(newScale)

                        offset = Offset(
                            (offset.x + pan.x).coerceIn(-max.x, max.x),
                            (offset.y + pan.y).coerceIn(-max.y, max.y)
                        )
                        scale = newScale
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1.1f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                scale = 2.5f
                                offset = Offset.Zero
                            }
                        }
                    )
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            contentScale = ContentScale.Fit
        )

        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp)
        ) {
            IconButton(modifier = Modifier.size(56.dp), onClick = { onBack() }) {
                Icon(Icons.Default.ArrowBack, "Назад", tint = Color.White)
            }
            Row(Modifier.weight(2.0f)) {}
            Box {
                IconButton(modifier = Modifier.size(56.dp), onClick = {
                    menuExpanded.value = true
                }) {
                    Icon(Icons.Default.MoreVert, "Ещё", tint = Color.White)
                }
                DropdownMenu(
                    expanded = menuExpanded.value,
                    onDismissRequest = { menuExpanded.value = false },
                    modifier = Modifier
                        .width(230.dp)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("Скачать", color = MaterialTheme.colorScheme.onBackground)
                        },
                        onClick = {
                            scope.launch {
                                FileSaver().saveBitmapImage(
                                    bitmap,
                                    "img${bitmap.hashCode() + Clock.System.now().hashCode()}.png"
                                )
                                snackbarHostState.showSnackbar("Изображение сохранено")
                            }
                            menuExpanded.value = false
                        }
                    )
                }
            }
        }
    }
}