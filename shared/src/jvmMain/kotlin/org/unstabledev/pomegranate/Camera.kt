package org.unstabledev.pomegranate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual object Camera{
    @Composable
    actual fun CameraPreview(modifier: Modifier) {
    }

    @Composable
    actual fun startCamera() {
    }

    actual fun takePhoto(): String {
        return "Camera"
    }

    actual fun videoStream(action: (bytes: ByteArray) -> Unit) {
    }

}