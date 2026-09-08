package org.unstabledev.pomegranate

import androidx.compose.runtime.Composable
import com.fleeksoft.io.ByteArrayInputStream
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import java.awt.datatransfer.Clipboard as SystemClipboard

@Composable
actual fun setStatusBarIcons(lightIcons: Boolean) { }

actual fun sendHaptic(amplitude: Int) { }
actual fun sendHaptic(milliseconds: Long, amplitude: Int) { }

actual class Clipboard {
    private val clipboard: java.awt.datatransfer.Clipboard =
        Toolkit.getDefaultToolkit().systemClipboard

    actual fun copyText(str: String) {
        val selection = StringSelection(str)
        clipboard.setContents(selection, selection)
        println("Text copied to clipboard")
    }

    actual fun copyImage(data: String) {
        try {
            val file=KMPFile(data)
            if (!file.exists()) {
                println("File does not exist: ${file.absolutePath}")
                return
            }
            if (!file.isFile()) {
                println("Path is not a file: ${file.absolutePath}")
                return
            }
            val javaFile = File(file.absolutePath)
            val bufferedImage = ImageIO.read(javaFile)
            if (bufferedImage == null) {
                println("Failed to read image from file")
                return
            }

            println("Image loaded: ${bufferedImage.width}x${bufferedImage.height}, type: ${bufferedImage.type}")
            val selection = ImageTransferable(bufferedImage)
            clipboard.setContents(selection, null)
            println("Image copied to clipboard successfully")
        } catch (e: Exception) {
            println("Failed to copy image from file: ${e.message}")
            e.printStackTrace()
        }
    }

    actual fun get(): ClipboardEntry? {
        getText()?.let { return ClipboardEntry.Text(it) }
        getImage()?.let { return ClipboardEntry.Image(it) }
        return null
    }
    actual fun getText(): String? {
        return try {
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                clipboard.getData(DataFlavor.stringFlavor) as? String
            } else {
                null
            }
        } catch (e: Exception) {
            println("Failed to get text: ${e.message}")
            null
        }
    }
    actual fun getImage(): ByteArray? {
        if (!clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
            println("No image available in clipboard")
            return null
        }

        return try {
            val image = clipboard.getData(DataFlavor.imageFlavor) as? BufferedImage
            if (image == null) {
                println("Failed to get BufferedImage from clipboard")
                return null
            }

            val bos = ByteArrayOutputStream()
            ImageIO.write(image, "png", bos)
            bos.toByteArray()
        } catch (e: Exception) {
            println("Failed to get image: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}

private class ImageTransferable(private val image: BufferedImage) : Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor)
    override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean =
        flavor == DataFlavor.imageFlavor
    override fun getTransferData(flavor: DataFlavor?): Any {
        if (flavor != DataFlavor.imageFlavor) throw UnsupportedFlavorException(flavor)
        return image
    }
}
