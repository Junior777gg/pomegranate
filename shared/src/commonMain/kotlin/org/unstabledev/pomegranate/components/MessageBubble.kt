package org.unstabledev.pomegranate.components

//import com.mikepenz.markdown.m3.Markdown
//import com.mikepenz.markdown.m3.markdownTypography
//import com.mikepenz.markdown.model.markdownAnnotator
//import com.mikepenz.markdown.model.markdownAnnotatorConfig
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.FileSaver
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.api.OpenGraphDescriptor
import org.unstabledev.pomegranate.api.OpenGraphParser
import org.unstabledev.pomegranate.database.MessageDC
import org.unstabledev.pomegranate.getBitmapFromBytes
import kotlin.time.Clock

@Composable
fun MessageBubble(
    message: MessageDC,
    setImagePreview: (MessageDC) -> Unit,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val menuOpen = remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start
    ) {
        val needPadding = message.type != MessageDC.IMAGE
        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { menuOpen.value = true },
                        onTap = { if (message.type == MessageDC.IMAGE) { setImagePreview(message) } }
                    )
                }
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isMine) 16.dp else 4.dp,
                        bottomEnd = if (message.isMine) 4.dp else 16.dp
                    )
                )
                .background(if (message.isMine) ColorTheme.MyMessageBubble else MaterialTheme.colorScheme.surface)
                .padding(horizontal = if (needPadding) 12.dp else 0.dp, vertical = if (needPadding) 8.dp else 0.dp)
                .pointerHoverIcon(if (message.type == MessageDC.IMAGE) PointerIcon.Hand else PointerIcon.Default)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when (message.type) {
                    MessageDC.TEXT -> {
                        val decodedText = remember(message.data) { message.data.decodeToString() }
                        Column(Modifier.weight(1f, fill = false)) {
                            Text(
                                text = decodedText,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 15.sp,
                            )
                            val previewableUrl = remember { Regex("https?://\\S+").find(decodedText)?.value }
                            val descriptor by produceState<OpenGraphDescriptor?>(null) {
                                if (previewableUrl!=null) {
                                    try {
                                        value = OpenGraphParser.parse(previewableUrl)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        value = null
                                    }
                                } else value = null
                            }
                            if(descriptor!=null) {
                                Spacer(Modifier.height(3.dp))
                                Box(Modifier.clip(RoundedCornerShape(12.dp)).fillMaxWidth()) {
                                    Column(
                                        Modifier.background(Color.Gray.copy(0.2f)).padding(4.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Row {
                                            Column(Modifier.weight(2.0f)) {
                                                Text(
                                                    descriptor!!.url?:previewableUrl!!,
                                                    color = MaterialTheme.colorScheme.onBackground.copy(
                                                        alpha = 0.7f
                                                    ),
                                                    fontSize = 10.sp
                                                )
                                                Text(
                                                    descriptor!!.title!!,
                                                    lineHeight = 16.sp,
                                                    fontSize = 16.sp,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                if(descriptor!!.description!=null) {
                                                    Text(
                                                        descriptor!!.description!!,
                                                        fontSize = 12.sp,
                                                        lineHeight = 12.sp,
                                                        color = MaterialTheme.colorScheme.onBackground.copy(
                                                            alpha = 0.8f
                                                        ),
                                                        overflow = TextOverflow.Ellipsis,
                                                        maxLines = 3
                                                    )
                                                }
                                            }
                                            if(descriptor!!.imageUrl!=null) {
                                                AsyncImage(
                                                    modifier = Modifier.weight(1.0f),
                                                    model=descriptor!!.imageUrl!!,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    MessageDC.IMAGE -> {
                        var bitmap by remember(message.key) {
                            mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
                        }
                        var ratio by remember(message.key) {
                            mutableStateOf<Float?>(null)
                        }

                        LaunchedEffect(message.key) {
                            val bmp = kotlinx.coroutines.withContext(Dispatchers.Default) {
                                getBitmapFromBytes(message.data)
                            }
                            bitmap = bmp
                            ratio = bmp.width.toFloat() / bmp.height.toFloat()
                        }

                        val clampedRatio = (ratio ?: 1f).coerceIn(0.5f, 2.0f)

                        Box(
                            modifier = Modifier
                                .widthIn(min = 120.dp, max = 260.dp)
                                .aspectRatio(clampedRatio)
                                .animateContentSize()
                        ) {
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap!!,
                                    contentDescription = null,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Gray.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.4f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(message.time, color = Color.White, fontSize = 11.sp)
                                    Spacer(Modifier.width(2.dp))
                                    Icon(
                                        modifier = Modifier.size(14.dp),
                                        imageVector = if (message.isDelivered || !message.isMine)
                                            Icons.Default.Check
                                        else Icons.Default.ArrowOutward,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    MessageDC.ANIMATED_IMAGE -> {
                        var bitmap by remember(message.key) {
                            mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
                        }
                        var ratio by remember(message.key) {
                            mutableStateOf<Float?>(null)
                        }

                        LaunchedEffect(message.key) {
                            val bmp = kotlinx.coroutines.withContext(Dispatchers.Default) {
                                getBitmapFromBytes(message.data)
                            }
                            bitmap = bmp
                            ratio = bmp.width.toFloat() / bmp.height.toFloat()
                        }

                        val clampedRatio = (ratio ?: 1f).coerceIn(0.5f, 2.0f)

                        Box(
                            modifier = Modifier
                                .widthIn(min = 120.dp, max = 260.dp)
                                .aspectRatio(clampedRatio)
                                .animateContentSize()
                        ) {
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap!!,
                                    contentDescription = null,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Gray.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.4f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(message.time, color = Color.White, fontSize = 11.sp)
                                    Spacer(Modifier.width(2.dp))
                                    Icon(
                                        modifier = Modifier.size(14.dp),
                                        imageVector = if (message.isDelivered || !message.isMine)
                                            Icons.Default.Check
                                        else Icons.Default.ArrowOutward,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    MessageDC.FILE -> {
                        val savedAlready = remember { mutableStateOf(false) }
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    scope.launch {
                                        FileSaver().saveBytes(
                                            message.data,
                                            "${message.hashCode() + Clock.System.now().hashCode()}.bin"
                                        )
                                        snackbarHostState.showSnackbar("Файл сохранён")
                                        savedAlready.value = true
                                    }
                                }
                        ) {
                            Row(
                                Modifier
                                    .background(Color.Gray.copy(alpha = 0.2f))
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    imageVector = if (savedAlready.value) Icons.Default.Check else Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Файл", color = MaterialTheme.colorScheme.onBackground)
                                Spacer(Modifier.width(2.dp))
                            }
                        }
                    }
                }

                if (message.type != MessageDC.IMAGE) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message.time,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            modifier = Modifier.size(15.dp),
                            imageVector = if (message.isDelivered || !message.isMine) Icons.Default.Check else Icons.Default.ArrowOutward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        if (menuOpen.value) {
            DropdownMenu(
                expanded = menuOpen.value,
                onDismissRequest = { menuOpen.value = false },
                modifier = Modifier
                    .width(230.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                if (message.type == MessageDC.TEXT) {
                    DropdownMenuItem(
                        text = { Text("Скопировать", color = MaterialTheme.colorScheme.onBackground) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        onClick = {
                            scope.launch {
                                clipboardManager.setText(AnnotatedString(message.data.decodeToString()))
                            }
                            menuOpen.value = false
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text("Сохранить", color = MaterialTheme.colorScheme.onBackground) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        onClick = {
                            scope.launch {
                                if (message.type == MessageDC.FILE) {
                                    FileSaver().saveBytes(
                                        message.data,
                                        "file${message.hashCode() + Clock.System.now().hashCode()}.bin"
                                    )
                                } else {
                                    val bitmap = getBitmapFromBytes(message.data)
                                    FileSaver().saveBitmapImage(
                                        bitmap,
                                        "img${bitmap.hashCode() + Clock.System.now().hashCode()}.png"
                                    )
                                }
                                snackbarHostState.showSnackbar(if (message.type == MessageDC.IMAGE) "Изображение сохранено" else "Файл сохранён")
                            }
                            menuOpen.value = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        scope.launch {
                            Repository.messagesDao.deleteMessage(message)
                        }
                        menuOpen.value = false
                    }
                )
            }
        }
    }
}