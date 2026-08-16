package org.unstabledev.pomegranate

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.OpenCVFrameGrabber
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

actual class Camera {
    val grabber = OpenCVFrameGrabber(0)
    val frameFlow = MutableSharedFlow<BufferedImage?>()
    val frameChannel = Channel<BufferedImage?>()

    @Composable
    actual fun CameraPreview(modifier: Modifier) {
        val bitmap = frameFlow.collectAsState(null).value?.toComposeImageBitmap() ?: return
        Image(
            modifier = modifier, bitmap = bitmap, contentDescription = null
        )
    }

    actual fun startCamera(front: Boolean) {
        grabber.start()
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val frame = grabber.grab()
                    frameChannel.send(Java2DFrameConverter().convert(frame))
                    frameFlow.emit(Java2DFrameConverter().convert(frame))
                    delay(30)
                } catch (_: Exception) {
                    grabber.stop()
                }
            }
        }
    }

    actual fun takePhoto(): String {
        val frame = grabber.grab()
        val image = Java2DFrameConverter().convert(frame)
        val jpegFile = File(
            "${Repository.pomegranatePath}temp",
            "photo_${System.currentTimeMillis()}.jpeg"
        )
        ImageIO.write(image, "jpeg", jpegFile)

        return jpegFile.absolutePath
    }


    actual fun switchView() {
    }

    actual fun switchView(front: Boolean) {
    }

    actual suspend fun getFrame(): ByteArray {
        val frame = frameChannel.receive()
        val stream = ByteArrayOutputStream()
        ImageIO.write(frame, "jpg", stream)
        return stream.toByteArray()
    }
}