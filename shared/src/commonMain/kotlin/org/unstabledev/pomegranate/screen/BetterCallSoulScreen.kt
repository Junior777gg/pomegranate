package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import org.unstabledev.pomegranate.Camera
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.screen.control.BetterCallSoulScreenController

@Composable
fun BetterCallSoulScreen(navWayObj: NavigationWays) {
    val viewModel = viewModel { BetterCallSoulScreenController() }
    Camera.startCamera()
    Camera.CameraPreview(modifier = Modifier.fillMaxSize())
}