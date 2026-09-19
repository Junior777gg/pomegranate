package org.unstabledev.pomegranate.platform

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import java.io.File
import java.io.FileOutputStream
import kotlin.jvm.java

actual val isMobile: Boolean
    get() = true

actual val handleTapGestures: Boolean
    get() = true

@Composable
actual fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

@Composable
actual fun setStatusBarIcons(lightIcons: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = lightIcons
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@RequiresPermission(Manifest.permission.VIBRATE)
actual fun sendHaptic(amplitude: Int) {
    sendHaptic(0, amplitude)
}

@RequiresApi(Build.VERSION_CODES.Q)
@RequiresPermission(Manifest.permission.VIBRATE)
actual fun sendHaptic(milliseconds: Long, amplitude: Int) {
    val vibrator = context?.getSystemService(Vibrator::class.java)
    if(amplitude in -1..5)
        vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    else
        vibrator?.vibrate(VibrationEffect.createOneShot(milliseconds, amplitude))
}

actual class Clipboard {
    private val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    private val authority = "${context?.packageName}.fileprovider"

    actual fun copyText(str: String) {
        try {
            clipboard?.setPrimaryClip(ClipData.newPlainText("Copied text", str))
            println("Text copied to clipboard")
        } catch (e: Exception) {
            println("Failed to copy text: ${e.message}")
            e.printStackTrace()
        }
    }

    actual fun copyImage(data: String) {
        try {
            val file= KMPFile(data)
            if (!file.exists()) {
                println("File does not exist: ${file.absolutePath}")
                return
            }
            if (!file.isFile) {
                println("Path is not a file: ${file.absolutePath}")
                return
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap == null) {
                println("Failed to decode bitmap from file")
                return
            }

            saveBitmapAndCopy(bitmap)
            bitmap.recycle()
        } catch (e: Exception) {
            println("Failed to copy image from file: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun saveBitmapAndCopy(bitmap: Bitmap) {
        try {
            val imagesFolder = File(context?.cacheDir, "shared_images")
            if (!imagesFolder.exists()) imagesFolder.mkdirs()

            imagesFolder.listFiles()?.forEach { file ->
                if (file.name.startsWith("copied_image_")) file.delete()
            }

            val timestamp = System.currentTimeMillis()
            val file = File(imagesFolder, "copied_image_$timestamp.png")
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            }

            if (!file.exists() || file.length() == 0L) {
                println("File was not created properly")
                return
            }

            val contentUri = FileProvider.getUriForFile(context!!, authority, file)

            val clip = ClipData.newUri(context?.contentResolver, "Image", contentUri)
            clipboard?.setPrimaryClip(clip)
            context?.grantUriPermission(
                "com.android.systemui",
                contentUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            println("Image copied to clipboard successfully")
        } catch (e: Exception) {
            println("Failed to save and copy bitmap: ${e.message}")
            e.printStackTrace()
        }
    }

    actual fun get(): ClipboardEntry? {
        val clip = clipboard?.primaryClip ?: return null
        val description = clip.description

        return when {
            description.hasMimeType("text/plain") || description.hasMimeType("text/html") ->
                getText()?.let { ClipboardEntry.Text(it) }
            description.hasMimeType("image/*") ->
                getImage()?.let { ClipboardEntry.Image(it) }
            else -> {
                val item = clip.getItemAt(0)
                if (item.uri != null) {
                    getImage()?.let { ClipboardEntry.Image(it) }
                } else {
                    null
                }
            }
        }
    }
    actual fun getText(): String? {
        val item = clipboard?.primaryClip?.getItemAt(0)
        return item?.text?.toString() ?: item?.coerceToText(context)?.toString()
    }
    actual fun getImage(): ByteArray? {
        val clip = clipboard?.primaryClip ?: return null
        val item = clip.getItemAt(0)
        val uri = item.uri ?: return null

        return try {
            context?.contentResolver?.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            println("Failed to retrieve image: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}

actual object Battery {
    private val batteryManager=context?.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val powerManager=context?.getSystemService(Context.POWER_SERVICE) as PowerManager
    actual fun isPowerSaveMode(): Boolean = powerManager.isPowerSaveMode
    actual fun getLevel(): Int {
        return try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            -1
        }
    }
    actual fun isCharging(): Boolean {
        val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }
}