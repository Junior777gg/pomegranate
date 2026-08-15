package org.unstabledev.pomegranate

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.OpenCVFrameGrabber
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

actual class Camera {
    val grabber = OpenCVFrameGrabber(0)
    val frameFlow = MutableSharedFlow<BufferedImage?>()

    @Composable
    actual fun CameraPreview(modifier: Modifier) {
        val bitmap = frameFlow.collectAsState(null).value?.toComposeImageBitmap() ?: return
        Image(
            modifier = modifier, bitmap = bitmap, contentDescription = null
        )
    }

    @Composable
    actual fun StartCamera(front: Boolean) {
        grabber.start()
        val scope = rememberCoroutineScope()
        scope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    val frame = grabber.grab()
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

    actual fun videoStream(action: (bytes: ByteArray) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            frameFlow.collect {
                val stream = ByteArrayOutputStream()
                ImageIO.write(it, "jpeg", stream)
                action(stream.toByteArray())
            }
        }
    }

    actual fun switchView() {
    }

    actual fun switchView(front: Boolean) {
    }
}