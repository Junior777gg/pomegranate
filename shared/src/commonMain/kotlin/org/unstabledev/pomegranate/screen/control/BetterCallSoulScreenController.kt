package org.unstabledev.pomegranate.screen.control

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
import org.unstabledev.pomegranate.Camera
import org.unstabledev.pomegranate.NavigationWays
import org.unstabledev.pomegranate.P2PUtils.Data
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.getBitmapFromBytes

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
                    delay(30)
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
