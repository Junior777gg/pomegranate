package org.unstabledev.pomegranate

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.OpenCVFrameGrabber
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

actual class Camera {
    val grabber = OpenCVFrameGrabber(0)
    val frameFlow = mutableStateOf<BufferedImage?>(null)

    @Composable
    actual fun CameraPreview(modifier: Modifier) {
        val bitmap = frameFlow.value?.toComposeImageBitmap()
        if (bitmap != null) {
            Image(
                modifier = modifier, bitmap = bitmap, contentDescription = null
            )
        }
    }

    actual fun startCamera(front: Boolean) {
        grabber.setImageWidth(640)
        grabber.setImageHeight(480)
        grabber.start()
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val frame = grabber.grab()
                    frameFlow.value = Java2DFrameConverter().convert(frame)
                    delay(100)
                } catch (_: Exception) {
                }
            }
        }
    }

    actual fun takePhoto(): String {
        val image = frameFlow.value
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
        val frame = frameFlow.value ?: return ByteArray(0)
        val stream = ByteArrayOutputStream()
        ImageIO.write(frame, "jpg", stream)
        return stream.toByteArray()
    }
}