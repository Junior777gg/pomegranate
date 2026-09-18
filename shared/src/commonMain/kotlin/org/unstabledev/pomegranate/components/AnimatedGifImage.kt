package org.unstabledev.pomegranate.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.collections.emptyList
import kotlin.time.Duration.Companion.milliseconds

data class GifFrame(
    val bitmap: ImageBitmap,
    val duration: Int
)

expect object GifDecoder {
    fun decode(bytes: ByteArray): List<GifFrame>
}

@Composable
fun AnimatedGifImage(
    bytes: ByteArray,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = null,
    animate: Boolean = true
) {
    val frames = remember(bytes) {
        try {
            GifDecoder.decode(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    if (frames.isEmpty()) {
        Box(modifier = modifier)
        return
    }

    if (frames.size == 1 || !animate) {
        Image(
            bitmap = frames[0].bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        AnimatedGif(
            frames = frames,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
fun AnimatedGif(
    frames: List<GifFrame>,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    var currentFrame = remember { mutableStateOf(0) }

    LaunchedEffect(frames) {
        while (isActive) {
            delay(frames[currentFrame.value].duration.toLong().milliseconds)
            currentFrame.value = (currentFrame.value + 1) % frames.size
        }
    }

    Image(
        bitmap = frames[currentFrame.value].bitmap,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale
    )
}