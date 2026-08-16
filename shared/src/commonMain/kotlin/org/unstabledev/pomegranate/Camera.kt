package org.unstabledev.pomegranate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect class Camera() {
    @Composable
    fun CameraPreview(modifier: Modifier)


    fun startCamera(front: Boolean = false)

    fun takePhoto(): String
    suspend fun getFrame(): ByteArray
    fun switchView()
    fun switchView(front: Boolean)
}