package org.unstabledev.pomegranate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect object Camera{
    @Composable
    fun CameraPreview(modifier: Modifier)
    @Composable
    fun startCamera()
    fun takePhoto(): String
    fun videoStream(action:(bytes: ByteArray) -> Unit)
}