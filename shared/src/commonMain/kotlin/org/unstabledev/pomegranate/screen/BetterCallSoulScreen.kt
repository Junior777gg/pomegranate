package org.unstabledev.pomegranate.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.CallState
import org.unstabledev.pomegranate.Camera
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.Repository.currentCallState
import org.unstabledev.pomegranate.applyScreenPadding
import org.unstabledev.pomegranate.components.ColorTheme
import org.unstabledev.pomegranate.screen.control.BetterCallSoulScreenController
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun BetterCallSoulScreen(navWayObj: NavigationWays) {
    val camera = remember { Camera() }
    val playbackDevice = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val viewModel = viewModel { BetterCallSoulScreenController(camera, navWayObj) }
    val state = currentCallState.value
    scope.launch(Dispatchers.IO) {
        viewModel.currentCallStateFlow.emit(currentCallState.value)
    }
    val microphoneActive = viewModel.microphoneActive
    val cameraActive = viewModel.cameraActive

    val elapsedSeconds = remember { mutableStateOf(0) }

    val hours = elapsedSeconds.value / 3600
    val minutes = (elapsedSeconds.value % 3600) / 60
    val seconds = elapsedSeconds.value % 60
    val timerText = buildString {
        if (hours > 0) {
            append(hours)
            append(':')
            append(minutes.toString().padStart(2, '0'))
        } else {
            append(minutes.toString().padStart(2, '0'))
        }
        append(':')
        append(seconds.toString().padStart(2, '0'))
    }
    LaunchedEffect(Unit) {
        camera.startCamera(true)
        while (true) {
            delay(1000.milliseconds)
            elapsedSeconds.value++
        }
    }
    when (state) {
        CallState.Calling -> {
            Column(verticalArrangement = Arrangement.Bottom) {
                Row(
                    applyScreenPadding(Modifier.fillMaxWidth()).padding(
                        bottom = 10.dp
                    ),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(Modifier.clip(CircleShape)) {
                        Column(
                            Modifier.background(ColorTheme.Warning).size(50.dp).clickable {
                                scope.launch {
                                    currentCallState.value = CallState.Cancelled
                                }
                            },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                modifier = Modifier.size(35.dp),
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                    Box(Modifier.clip(CircleShape)) {
                        Column(
                            Modifier.background(ColorTheme.Warning).size(50.dp).clickable {
                                scope.launch {
                                    currentCallState.value = CallState.AcceptedCall
                                }
                            },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                modifier = Modifier.size(35.dp),
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        CallState.AcceptedCall -> {

            Box {
                val bitmap = viewModel.image.value
                if (bitmap != null) {
                    Image(modifier = Modifier.fillMaxSize(), bitmap = bitmap, contentDescription = null)
                    camera.CameraPreview(modifier = Modifier.size(100.dp))
                } else {
                    camera.CameraPreview(modifier = Modifier.fillMaxSize())
                }
                Row(
                    applyScreenPadding(Modifier.fillMaxWidth().align(Alignment.TopCenter)),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(Modifier.clip(RoundedCornerShape(16.dp))) {
                        Column(
                            Modifier.background(Color.Black.copy(alpha = 0.2f)).padding(5.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(timerText)
                        }
                    }
                }
                Row(
                    applyScreenPadding(Modifier.fillMaxWidth().align(Alignment.TopCenter)).padding(end = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        modifier = Modifier.size(35.dp).clickable {
                            camera.switchView()
                        },
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Row(
                    applyScreenPadding(Modifier.fillMaxWidth().align(Alignment.BottomCenter)).padding(
                        bottom = 10.dp
                    ),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(Modifier.clip(CircleShape)) {
                        Column(
                            Modifier.background(ColorTheme.Warning).size(50.dp).clickable {
                                scope.launch {
                                    currentCallState.value = CallState.Cancelled
                                }
                            },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                modifier = Modifier.size(35.dp),
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                    Box(Modifier.clip(CircleShape)) {
                        Column(
                            Modifier.background(
                                if (microphoneActive.value) Color.Black.copy(alpha = 0.2f) else Color.White
                            ).size(50.dp).clickable { microphoneActive.value = !microphoneActive.value },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                modifier = Modifier.size(35.dp),
                                imageVector = if (microphoneActive.value) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = null,
                                tint = if (microphoneActive.value) Color.White else Color.Black
                            )
                        }
                    }
                    Box(Modifier.clip(CircleShape)) {
                        Column(
                            Modifier.background(
                                if (cameraActive.value) Color.Black.copy(alpha = 0.2f) else Color.White
                            ).size(50.dp).clickable { cameraActive.value = !cameraActive.value },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                modifier = Modifier.size(35.dp),
                                imageVector = if (cameraActive.value) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = null,
                                tint = if (cameraActive.value) Color.White else Color.Black
                            )
                        }
                    }
                    Box(Modifier.clip(CircleShape)) {
                        Column(
                            Modifier.background(Color.Black.copy(alpha = 0.2f)).size(50.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                modifier = Modifier.size(35.dp),
                                imageVector = Icons.Default.Money,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        else -> {
            camera.CameraPreview(Modifier.fillMaxSize())
        }
    }
}