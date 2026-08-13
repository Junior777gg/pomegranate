package org.unstabledev.pomegranate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect class Camera() {
    @Composable
    fun CameraPreview(modifier: Modifier)

    @Composable
    fun StartCamera(front: Boolean = false)

    fun takePhoto(): String
    fun videoStream(action: (bytes: ByteArray) -> Unit)
    fun switchView()
    fun switchView(front: Boolean)
}