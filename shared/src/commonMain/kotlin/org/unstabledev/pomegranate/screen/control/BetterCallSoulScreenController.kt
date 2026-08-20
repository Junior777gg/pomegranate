package org.unstabledev.pomegranate.screen.control

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.http.cio.decodeChunked
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.CallAudioPlayer
import org.unstabledev.pomegranate.CallAudioRecorder
import org.unstabledev.pomegranate.Camera
import org.unstabledev.pomegranate.P2PUtils.Data
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.getBitmapFromBytes

class BetterCallSoulScreenController(val camera: Camera) : ViewModel() {
    val call = Repository.currentCallState.value!!
    val image = mutableStateOf<ImageBitmap?>(null)
    val recorder = CallAudioRecorder()
    val player = CallAudioPlayer()

    init {
        val audioManager = call.manager.fork()
        recorder.start()
        player.start()
        println("betterCallSoulScreenController")
        viewModelScope.launch(Dispatchers.IO) {
            while (call.manager.channel == null && audioManager.channel == null) {
                delay(100)
            }
            println("notnull")
            val videoChannel = call.manager.channel!!
            val audioChannel = audioManager.channel!!
            launch {
                while (true) {
                    val data = audioChannel.receive()
                    player.playChunk((data as Data.Bytes).bytes)
                }
            }
            launch {
                while (true) {
                    val frame = recorder.getFrame()
                    if (frame != null) {
                        videoChannel.send(frame)
                    }
                }
            }
            launch {
                while (true) {
                    val data = videoChannel.receive()
                    val bitmap = getBitmapFromBytes((data as Data.Bytes).bytes)
                    image.value = bitmap
                }
            }
            launch {
                while (true) {
                    val frame = camera.getFrame()
                    if (frame != null) {
                        videoChannel.send(frame)
                    }
                    delay(100)
                }
            }
        }
    }
}