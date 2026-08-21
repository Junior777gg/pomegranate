package org.unstabledev.pomegranate.screen.control

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.unstabledev.pomegranate.CallAudioPlayer
import org.unstabledev.pomegranate.CallAudioRecorder
import org.unstabledev.pomegranate.CallState
import org.unstabledev.pomegranate.Camera
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.P2PUtils.Data
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Repository.currentCallState
import org.unstabledev.pomegranate.getBitmapFromBytes

class BetterCallSoulScreenController(
    val camera: Camera,
    val navWayObj: NavigationWays,
    val microphoneActive: MutableState<Boolean>,
    val cameraActive: MutableState<Boolean>
) : ViewModel() {
    val call = Repository.currentCall.value!!
    val image = mutableStateOf<ImageBitmap?>(null)
    val recorder = CallAudioRecorder()
    val player = CallAudioPlayer()

    init {
        val audioManager = call.audioManager
        val videoManager = call.videoManager
        recorder.start()
        player.start()
        viewModelScope.launch(Dispatchers.IO) {
            currentCallState.collect { state ->
                when (state) {
                    CallState.AcceptedCall -> {
                        launch {
                            while (videoManager.channel == null && audioManager.channel == null) {
                                delay(100)
                            }
                            launch {
                                while (true) {
                                    val data = audioManager.channel!!.receive()
                                    player.playChunk((data as Data.Bytes).bytes)
                                }
                            }
                            launch {
                                while (microphoneActive.value) {
                                    val frame = recorder.getFrame()
                                    audioManager.channel!!.send(frame)
                                }
                            }
                            launch {
                                while (true) {
                                    val data = videoManager.channel!!.receive()
                                    val bitmap = getBitmapFromBytes((data as Data.Bytes).bytes)
                                    image.value = bitmap
                                }
                            }
                            launch {
                                while (cameraActive.value) {
                                    val frame = camera.getFrame()
                                    videoManager.channel!!.send(frame)
                                    delay(100)
                                }
                            }
                        }
                    }

                    CallState.Cancelled -> {
                        launch {
                            player.stop()
                            recorder.stop()
                            withContext(Dispatchers.Main) {
                                navWayObj.back()
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}