package org.unstabledev.pomegranate.screen.control

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.unstabledev.pomegranate.platform.CallAudioPlayer
import org.unstabledev.pomegranate.platform.CallAudioRecorder
import org.unstabledev.pomegranate.platform.Camera
import org.unstabledev.pomegranate.screen.nav.NavigationWays
import org.unstabledev.pomegranate.P2PUtils.Data
import org.unstabledev.pomegranate.Repository

class BetterCallSoulScreenController(
    val camera: Camera,
    val navWayObj: NavigationWays,
) : ViewModel() {
    val call = Repository.currentCall.value!!
    val image = mutableStateOf<ImageBitmap?>(null)
    val recorder = CallAudioRecorder()
    val player = CallAudioPlayer()
    val microphoneActive = mutableStateOf(true)
    val cameraActive = mutableStateOf(true)
    val audioManager = call.audioManager
    val videoManager = call.videoManager

    fun acceptedCall() {
        viewModelScope.launch(Dispatchers.IO) {
            recorder.start()
            player.start()
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
                while (true) {
                    if (microphoneActive.value) {
                        val frame = recorder.getFrame()
                        audioManager.channel!!.send(frame)
                    }
                }
            }
            launch {
                while (true) {
                    val data = videoManager.channel!!.receive()
                    val bitmap = try {
                        val copy = (data as Data.Bytes).bytes.copyOf()
                        copy.decodeToImageBitmap()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                    image.value = bitmap
                }
            }
            launch {
                while (true) {
                    if (cameraActive.value) {
                        val frame = camera.getFrame()
                        videoManager.channel!!.send(frame)
                    }
                    delay(33)
                }
            }

        }
    }

    fun cancel() {
        viewModelScope.launch(Dispatchers.IO) {
            player.stop()
            recorder.stop()
            withContext(Dispatchers.Main) {
                navWayObj.back()
            }
        }
    }
}
