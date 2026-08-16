package org.unstabledev.pomegranate.screen.control

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.unstabledev.pomegranate.Camera
import org.unstabledev.pomegranate.P2PUtils.Data
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.getBitmapFromBytes

class BetterCallSoulScreenController(val camera: Camera) : ViewModel() {
    val call = Repository.currentCallState.value!!
    val image = mutableStateOf<ImageBitmap?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            while (call.manager.channel == null) {
                delay(100)
                println(null)
            }
            val channel = call.manager.channel!!
            launch {
                while (true) {
                    val data = channel.receive()
                    println((data as Data.Bytes).bytes.size)
                    val bitmap = getBitmapFromBytes((data as Data.Bytes).bytes)
                    image.value = bitmap
                }
            }
            launch {
                camera.videoStream {
                    launch {
                        println(it.size)
                        channel.send(it)
                    }
                }
            }
        }
    }
}