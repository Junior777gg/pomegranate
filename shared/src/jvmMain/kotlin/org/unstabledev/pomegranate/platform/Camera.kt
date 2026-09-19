package org.unstabledev.pomegranate.platform

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.OpenCVFrameGrabber
import org.unstabledev.pomegranate.Repository


import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

actual class Camera {
    val converter = Java2DFrameConverter()
    val grabber = OpenCVFrameGrabber(0)

    @Volatile
    var frame: Frame? = null

    @Composable
    actual fun CameraPreview(modifier: Modifier) {
        val currentFrame = remember { mutableStateOf<ImageBitmap?>(null) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                while (isActive) {
                    if (frame != null) {
                        currentFrame.value = converter.convert(frame).toComposeImageBitmap()
                    }
                    delay(33)
                }
            }
        }
        if (currentFrame.value != null) {
            Image(modifier = modifier, bitmap = currentFrame.value!!, contentDescription = null)
        }
    }

    actual fun takePhoto(): String {
        val image = converter.convert(frame)
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
        val frame = converter.convert(frame)
        val stream = ByteArrayOutputStream()
        val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
        val ios = ImageIO.createImageOutputStream(stream)
        writer.output = ios
        val param = writer.defaultWriteParam.apply {
            compressionMode = ImageWriteParam.MODE_EXPLICIT
            compressionQuality = 0.3f
        }
        val currentFrame = BufferedImage(480, 320, BufferedImage.TYPE_INT_RGB)
        val graphics = currentFrame.createGraphics()
        graphics.drawImage(frame, 0, 0, 480, 320, null)
        writer.write(null, IIOImage(currentFrame, null, null), param)
        graphics.dispose()
        writer.dispose()
        ios.close()
        return stream.toByteArray()
    }

    actual fun startCamera(lifeOwner: LifecycleOwner, front: Boolean) {
        grabber.start()
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    frame = grabber.grab()
                    delay(33)
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }
}