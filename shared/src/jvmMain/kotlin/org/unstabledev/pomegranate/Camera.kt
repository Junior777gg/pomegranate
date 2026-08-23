package org.unstabledev.pomegranate

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
        val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
        val ios = ImageIO.createImageOutputStream(stream)
        writer.output = ios
        val param = writer.defaultWriteParam.apply {
            compressionMode = ImageWriteParam.MODE_EXPLICIT
            compressionQuality = 0.2f
        }
        val currentFrame = BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB)
        val graphics = currentFrame.createGraphics()
        graphics.drawImage(frame, 0, 0, 640, 480, null)
        graphics.dispose()
        writer.write(null, IIOImage(currentFrame, null, null), param)
        ios.close()
        return stream.toByteArray()
    }

    actual fun startCamera(lifeOwner: LifecycleOwner, front: Boolean) {
        grabber.start()
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val frame = grabber.grab()
                    frameFlow.value = Java2DFrameConverter().convert(frame)
                    delay(30)
                } catch (_: Exception) {
                }
            }
        }
    }
}