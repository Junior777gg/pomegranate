package org.unstabledev.pomegranate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual class Camera {
    @Composable
    actual fun CameraPreview(modifier: Modifier) {
    }

    @Composable
    actual fun StartCamera(front: Boolean) {
    }

    actual fun takePhoto(): String {
        return "Camera"
    }

    actual fun videoStream(action: (bytes: ByteArray) -> Unit) {
    }

    actual fun switchView() {
    }

    actual fun switchView(front: Boolean) {
    }
}