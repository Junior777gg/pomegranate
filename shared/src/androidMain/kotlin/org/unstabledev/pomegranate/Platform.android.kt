package org.unstabledev.pomegranate

import android.Manifest
import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import kotlin.jvm.java

actual val isMobile: Boolean
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
    sendHaptic(0,amplitude)
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