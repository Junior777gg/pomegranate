package org.unstabledev.pomegranate.components

import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode

actual object GifDecoder {
    actual fun decode(bytes: ByteArray): List<GifFrame> {
        return try {
            decodeWithSkiaCodec(bytes) ?: decodeWithImageIO(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun decodeWithSkiaCodec(bytes: ByteArray): List<GifFrame>? {
        return try {
            val codec = Codec.makeFromData(Data.makeFromBytes(bytes))
            val frames = mutableListOf<GifFrame>()

            for (i in 0 until codec.frameCount) {
                val frameInfo = codec.framesInfo[i]
                val bitmap = Bitmap()
                bitmap.allocPixels(codec.imageInfo)
                codec.readPixels(bitmap, i)

                frames.add(
                    GifFrame(
                        bitmap = bitmap.asComposeImageBitmap(),
                        duration = frameInfo.duration
                    )
                )
            }

            frames
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun decodeWithImageIO(bytes: ByteArray): List<GifFrame> {
        val frames = mutableListOf<GifFrame>()

        try {
            val inputStream = ByteArrayInputStream(bytes)
            val readers = ImageIO.getImageReadersByFormatName("gif")

            if (!readers.hasNext()) {
                val image = ImageIO.read(ByteArrayInputStream(bytes))
                if (image != null) {
                    frames.add(
                        GifFrame(
                            bitmap = image.toComposeImageBitmap(),
                            duration = 0
                        )
                    )
                }
                return frames
            }

            val reader = readers.next() as ImageReader
            val imageInputStream = ImageIO.createImageInputStream(inputStream)
            reader.input = imageInputStream

            val numImages = reader.getNumImages(true)

            for (i in 0 until numImages) {
                val image = reader.read(i)
                val metadata = reader.getImageMetadata(i)
                val duration = extractFrameDuration(metadata)

                frames.add(
                    GifFrame(
                        bitmap = image.toComposeImageBitmap(),
                        duration = duration
                    )
                )
            }

            reader.dispose()
            imageInputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return frames
    }

    private fun extractFrameDuration(metadata: IIOMetadata): Int {
        return try {
            val metaFormatName = metadata.nativeMetadataFormatName
            val root = metadata.getAsTree(metaFormatName) as IIOMetadataNode
            val graphicsControlExtensionNode = getNode(root, "GraphicControlExtension")

            val delayTime = graphicsControlExtensionNode?.getAttribute("delayTime")?.toIntOrNull() ?: 10
            delayTime * 10
        } catch (_: Exception) {
            100
        }
    }

    private fun getNode(rootNode: IIOMetadataNode, nodeName: String): IIOMetadataNode? {
        val nodeList: NodeList = rootNode.getElementsByTagName(nodeName)
        return if (nodeList.length > 0) {
            nodeList.item(0) as IIOMetadataNode
        } else {
            null
        }
    }
}