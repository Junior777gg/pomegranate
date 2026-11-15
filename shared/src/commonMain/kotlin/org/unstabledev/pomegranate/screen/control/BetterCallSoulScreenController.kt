package org.unstabledev.pomegranate.screen.control

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.unstabledev.pomegranate.P2PUtils.Bytes
import org.unstabledev.pomegranate.platform.CallAudioPlayer
import org.unstabledev.pomegranate.platform.CallAudioRecorder
import org.unstabledev.pomegranate.platform.Camera
import org.unstabledev.pomegranate.Repository

class BetterCallSoulScreenController(
    val camera: Camera,
    val navController: NavHostController,
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
                audioManager.channel!!.receiver.collect {
                    player.playChunk((it[0] as Bytes).bytes)
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
                videoManager.channel!!.receiver.collect {
                    val bitmap = try {
                        val copy = (it[0] as Bytes).bytes.decodeToImageBitmap()
                        copy
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
                navController.popBackStack()
            }
        }
    }
}
