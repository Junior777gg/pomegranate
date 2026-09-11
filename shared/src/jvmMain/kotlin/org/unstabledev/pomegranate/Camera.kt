package org.unstabledev.pomegranate

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
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
    val frameFlow = mutableStateOf<ByteArray?>(null)

    @Composable
    actual fun CameraPreview(modifier: Modifier) {
        val scope = rememberCoroutineScope()
        val bitmap = remember { mutableStateOf<ImageBitmap?>(null) }
        scope.launch {
            bitmap.value = getBitmapFromBytesAsync(frameFlow.value ?: byteArrayOf())
        }
        if (bitmap.value != null) {
            Image(
                modifier = modifier, bitmap = bitmap.value!!, contentDescription = null
            )
        }
    }

    actual fun takePhoto(): String {
        val image = frameFlow.value
        val jpegFile = File(
            "${Repository.pomegranatePath}temp",
            "photo_${System.currentTimeMillis()}.jpeg"
        )
        //ImageIO.write(image, "jpeg", jpegFile)

        return jpegFile.absolutePath
    }


    actual fun switchView() {
    }

    actual fun switchView(front: Boolean) {
    }

    actual suspend fun getFrame(): ByteArray {
        return frameFlow.value ?: byteArrayOf()
    }

    actual fun startCamera(lifeOwner: LifecycleOwner, front: Boolean) {
        grabber.start()
        CoroutineScope(Dispatchers.IO).launch {
            val converter = Java2DFrameConverter()
            while (true) {
                try {
                    val frame = converter.convert(grabber.grab())
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
                    writer.write(null, IIOImage(currentFrame, null, null), param)
                    graphics.dispose()
                    writer.dispose()
                    ios.close()
                    frameFlow.value = stream.toByteArray()
                    delay(100)
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }
}